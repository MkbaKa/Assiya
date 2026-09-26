package github.mkbaka.bukkitksp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import github.mkbaka.bukkitksp.annotation.LifeCycle

class BukkitKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private var invoked = false
    private var pluginClassCache: List<KSClassDeclaration>? = null

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        invoked = true

        if (!validateOptionKeys()) return emptyList()

        val listeners = collectListeners(resolver)
        val awakes = collectAwakes(resolver)
        val customTasks = collectCustomTasks(resolver)
        val customFields = collectCustomFields(resolver)
        val hasAnnotations =
            listeners.isNotEmpty() || awakes.isNotEmpty() || customTasks.isNotEmpty() || customFields.isNotEmpty()

        val generatedPackage = resolveGeneratedPackage(resolver, hasAnnotations) ?: return emptyList()

        // 分发对象已在编译 classpath 上 (典型: kspTest 编译看见 main 的产物) 时不再生成,
        // 否则同 FQN 的空骨架会在测试 classpath 上遮蔽 main 产物.
        val existing = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("$generatedPackage.$AWAKE_LIFECYCLE_NAME")
        )
        if (existing != null) {
            if (hasAnnotations) {
                logger.warn(
                    "bukkit-ksp annotations in this compilation are ignored: " +
                        "dispatch objects already exist in '$generatedPackage'"
                )
            }
            return emptyList()
        }

        // 实例表达式在探测门之后解析: kspTest 编译先被探测门跳过, 不因测试源无主类误报;
        // 只在有消费方 (监听/生命周期/任务) 时才解析, CustomFields 与空骨架不需要插件实例.
        val pluginInstance =
            if (listeners.isNotEmpty() || awakes.isNotEmpty() || customTasks.isNotEmpty()) {
                resolvePluginInstance(resolver) ?: return emptyList()
            } else ""

        val nameErrors = validateListenerObjectNames(listeners)
        for (error in nameErrors) {
            logger.error(error)
        }
        if (nameErrors.isNotEmpty()) return emptyList()

        if (listeners.isNotEmpty()) {
            writeFile(
                generatedPackage, BUKKIT_LISTENERS_NAME,
                listeners.mapNotNull { it.sourceFile },
                renderBukkitListeners(generatedPackage, pluginInstance, listeners),
            )
        }
        writeFile(
            generatedPackage, AWAKE_LIFECYCLE_NAME,
            awakes.mapNotNull { it.sourceFile } + listeners.mapNotNull { it.sourceFile },
            renderAwakeLifecycle(
                generatedPackage,
                pluginInstance,
                awakes + syntheticListenerAwakes(generatedPackage, listeners)
            ),
        )
        writeFile(
            generatedPackage, CUSTOM_TASKS_NAME,
            customTasks.mapNotNull { it.sourceFile },
            renderCustomTasks(generatedPackage, pluginInstance, customTasks),
        )
        writeFile(
            generatedPackage, CUSTOM_FIELDS_NAME,
            customFields.mapNotNull { it.sourceFile },
            renderCustomFields(generatedPackage, customFields),
        )

        return emptyList()
    }

    private fun collectListeners(resolver: Resolver): List<ListenerModel> {
        val methodsByObject = linkedMapOf<String, Pair<KSClassDeclaration, MutableList<EventHandlerModel>>>()

        for (func in resolver.getSymbolsWithAnnotation(LISTENER_FQN).filterIsInstance<KSFunctionDeclaration>()) {
            val parent = requireObjectParent(func, "@Listener") ?: continue

            if (Modifier.PRIVATE in func.modifiers) {
                logger.error("@Listener method must not be private", func)
                continue
            }
            if (func.parameters.size != 1) {
                logger.error("@Listener method must have exactly 1 parameter", func)
                continue
            }

            val paramType = func.parameters[0].type.resolve()
            val eventTypeName = paramType.declaration.qualifiedName?.asString()
            if (eventTypeName == null) {
                logger.error("Cannot resolve event type", func)
                continue
            }

            val annotation = func.findAnnotation(LISTENER_FQN)!!
            val handler = EventHandlerModel(
                functionName = func.simpleName.asString(),
                eventTypeName = eventTypeName,
                priority = annotation.enumArgument("priority", func) ?: "NORMAL",
                ignoreCancelled = annotation.boolArgument("ignoreCancelled", func) ?: false,
            )

            val objectFqn = parent.qualifiedName!!.asString()
            methodsByObject.getOrPut(objectFqn) { parent to mutableListOf() }.second.add(handler)
        }

        return methodsByObject.map { (_, pair) ->
            val (classDecl, handlers) = pair
            ListenerModel(
                objectName = classDecl.qualifiedName!!.asString(),
                objectSimpleName = classDecl.simpleName.asString(),
                sourceFile = classDecl.containingFile,
                handlers = handlers,
            )
        }
    }

    private fun collectAwakes(resolver: Resolver): List<AwakeModel> {
        return resolver.getSymbolsWithAnnotation(AWAKE_FQN)
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { func ->
                val parent = requireObjectParent(func, "@Awake") ?: return@mapNotNull null

                if (Modifier.PRIVATE in func.modifiers) {
                    logger.error("@Awake method must not be private", func)
                    return@mapNotNull null
                }
                if (func.parameters.isNotEmpty()) {
                    logger.error("@Awake method must have no parameters", func)
                    return@mapNotNull null
                }

                val annotation = func.findAnnotation(AWAKE_FQN)!!
                if (annotation.argumentValue("lifeCycle") == null) {
                    logger.error("@Awake requires lifeCycle parameter", func)
                    return@mapNotNull null
                }
                val lifeCycle = annotation.enumArgument("lifeCycle", func) ?: return@mapNotNull null
                AwakeModel(
                    objectName = parent.qualifiedName!!.asString(),
                    functionName = func.simpleName.asString(),
                    lifeCycle = LifeCycle.valueOf(lifeCycle),
                    priority = annotation.intArgument("priority", func) ?: 0,
                    sourceFile = func.containingFile,
                )
            }
            .toList()
    }

    private fun collectCustomTasks(resolver: Resolver): List<CustomTaskModel> {
        return resolver.getSymbolsWithAnnotation(CUSTOM_TASK_FQN)
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { func ->
                val parent = requireObjectParent(func, "@CustomTask") ?: return@mapNotNull null

                if (Modifier.PRIVATE in func.modifiers) {
                    logger.error("@CustomTask method must not be private", func)
                    return@mapNotNull null
                }
                if (func.parameters.isNotEmpty()) {
                    logger.error("@CustomTask method must have no parameters", func)
                    return@mapNotNull null
                }

                val annotation = func.findAnnotation(CUSTOM_TASK_FQN)!!
                val taskId = annotation.stringArgument("taskId", func)
                if (taskId == null) {
                    logger.error("@CustomTask requires taskId parameter", func)
                    return@mapNotNull null
                }

                CustomTaskModel(
                    objectName = parent.qualifiedName!!.asString(),
                    functionName = func.simpleName.asString(),
                    taskId = taskId,
                    priority = annotation.intArgument("priority", func) ?: 0,
                    sourceFile = func.containingFile,
                )
            }
            .toList()
    }

    private fun collectCustomFields(resolver: Resolver): List<CustomFieldModel> {
        return resolver.getSymbolsWithAnnotation(CUSTOM_FIELD_FQN)
            .filterIsInstance<KSPropertyDeclaration>()
            .mapNotNull { prop ->
                val parent = requireObjectParent(prop, "@CustomField") ?: return@mapNotNull null

                if (Modifier.PRIVATE in prop.modifiers) {
                    logger.error("@CustomField property must not be private", prop)
                    return@mapNotNull null
                }

                val annotation = prop.findAnnotation(CUSTOM_FIELD_FQN)!!
                val fieldType = annotation.stringArgument("fieldType", prop)
                if (fieldType == null) {
                    logger.error("@CustomField requires fieldType parameter", prop)
                    return@mapNotNull null
                }

                CustomFieldModel(
                    objectName = parent.qualifiedName!!.asString(),
                    propertyName = prop.simpleName.asString(),
                    fieldType = fieldType,
                    sourceFile = prop.containingFile,
                )
            }
            .toList()
    }

    private fun validateOptionKeys(): Boolean {
        val unknown = options.keys.filter { it.startsWith(OPTION_PREFIX) && it !in KNOWN_OPTIONS }
        for (key in unknown) {
            logger.error("Unknown ksp option '$key'; known options: ${KNOWN_OPTIONS.sorted().joinToString(", ")}")
        }
        return unknown.isEmpty()
    }

    private fun pluginClasses(resolver: Resolver): List<KSClassDeclaration> {
        return pluginClassCache ?: resolver.getNewFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS && it.extendsJavaPlugin() }
            .toList()
            .also { pluginClassCache = it }
    }

    // 参数优先; 缺省时从本编译单元唯一的 JavaPlugin 子类推导生成包 (主类包名天然每插件唯一).
    // 零候选且无注解符号时静默跳过 (典型: kspTest 编译), 空骨架遮蔽 main 产物从此结构性不可能.
    private fun resolveGeneratedPackage(resolver: Resolver, hasAnnotations: Boolean): String? {
        options[GENERATED_PACKAGE_OPTION]?.let { return it }

        val plugins = pluginClasses(resolver)
        if (plugins.size > 1) {
            val fqns = plugins.joinToString(", ") { it.qualifiedName!!.asString() }
            logger.error(
                "Multiple JavaPlugin subclasses in this compilation ($fqns); " +
                    "specify ksp option '$GENERATED_PACKAGE_OPTION' to pick the generated package"
            )
            return null
        }

        val plugin = plugins.singleOrNull()
        if (plugin == null) {
            if (hasAnnotations) {
                logger.error(
                    "bukkit-ksp annotations found but no JavaPlugin subclass in this compilation; " +
                        "specify ksp option '$GENERATED_PACKAGE_OPTION'"
                )
            }
            return null
        }
        return plugin.packageName.asString()
    }

    // 实例表达式: 参数优先; 缺省时从唯一主类的伴生对象里找类型为主类自身的非 private 属性 (INSTANCE 惯例).
    private fun resolvePluginInstance(resolver: Resolver): String? {
        options[PLUGIN_INSTANCE_OPTION]?.let { return it }

        val plugins = pluginClasses(resolver)
        val mainClass = plugins.singleOrNull()
        if (mainClass == null) {
            logger.error(
                if (plugins.isEmpty()) {
                    "No JavaPlugin subclass in this compilation to derive the plugin instance accessor from; " +
                        "specify ksp option '$PLUGIN_INSTANCE_OPTION'"
                } else {
                    "Multiple JavaPlugin subclasses in this compilation; " +
                        "specify ksp option '$PLUGIN_INSTANCE_OPTION'"
                }
            )
            return null
        }

        val mainName = mainClass.qualifiedName!!.asString()
        val candidates = mainClass.declarations
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.isCompanionObject }
            ?.declarations
            ?.filterIsInstance<KSPropertyDeclaration>()
            ?.filter { Modifier.PRIVATE !in it.modifiers }
            ?.filter { (it.type.resolve().declaration as? KSClassDeclaration)?.qualifiedName?.asString() == mainName }
            ?.toList()
            .orEmpty()

        return when (candidates.size) {
            1 -> "$mainName.${candidates[0].simpleName.asString()}"
            0 -> {
                logger.error(
                    "No companion property of type ${mainClass.simpleName.asString()} on $mainName; " +
                        "declare e.g. 'companion object { lateinit var INSTANCE: ... }' assigned in init, " +
                        "or specify ksp option '$PLUGIN_INSTANCE_OPTION'"
                )
                null
            }

            else -> {
                logger.error(
                    "Multiple companion properties of type ${mainClass.simpleName.asString()} on $mainName " +
                        "(${candidates.joinToString(", ") { it.simpleName.asString() }}); " +
                        "specify ksp option '$PLUGIN_INSTANCE_OPTION'"
                )
                null
            }
        }
    }

    private fun KSClassDeclaration.extendsJavaPlugin(): Boolean {
        for (superRef in superTypes) {
            val superDecl = superRef.resolve().declaration as? KSClassDeclaration ?: continue
            if (superDecl.classKind != ClassKind.CLASS) continue
            val fqn = superDecl.qualifiedName?.asString() ?: continue
            if (fqn == JAVA_PLUGIN_FQN) return true
            if (fqn.startsWith("kotlin.") || fqn.startsWith("java.")) continue
            if (superDecl.extendsJavaPlugin()) return true
        }
        return false
    }

    private fun requireObjectParent(declaration: KSDeclaration, annotationName: String): KSClassDeclaration? {
        val parent = declaration.parentDeclaration as? KSClassDeclaration
        if (parent == null || parent.classKind != ClassKind.OBJECT || parent.isCompanionObject) {
            logger.error("$annotationName can only be used on members of object declarations", declaration)
            return null
        }
        if (Modifier.PRIVATE in parent.modifiers) {
            logger.error("$annotationName: containing object must not be private", parent)
            return null
        }
        return parent
    }

    private fun syntheticListenerAwakes(generatedPackage: String, listeners: List<ListenerModel>): List<AwakeModel> {
        if (listeners.isEmpty()) return emptyList()
        val listenersObject = "$generatedPackage.$BUKKIT_LISTENERS_NAME"
        return listOf(
            AwakeModel(
                objectName = listenersObject,
                functionName = "register",
                lifeCycle = LifeCycle.ENABLE,
                priority = LISTENER_REGISTER_PRIORITY,
                sourceFile = null,
            ),
            AwakeModel(
                objectName = listenersObject,
                functionName = "unregister",
                lifeCycle = LifeCycle.DISABLE,
                priority = LISTENER_UNREGISTER_PRIORITY,
                sourceFile = null,
            ),
        )
    }

    private fun writeFile(generatedPackage: String, fileName: String, sourceFiles: List<KSFile>, code: String) {
        codeGenerator.createNewFile(
            Dependencies(true, *sourceFiles.toTypedArray()),
            generatedPackage,
            fileName,
        ).bufferedWriter().use { it.write(code) }
    }

    private fun KSAnnotation.argumentValue(name: String): Any? =
        arguments.find { it.name?.asString() == name }?.value

    // KSP2: 注解声明缺省值经 defaultArguments 读不到, 实参缺失一律返回 null 由调用侧回落;
    // 实参存在但形态意外时显式报错, 不静默降级.
    private fun KSAnnotation.enumArgument(name: String, node: KSNode): String? {
        val value = argumentValue(name) ?: return null
        if (value !is KSClassDeclaration) {
            logger.error("Unexpected shape for enum argument '$name': ${value.javaClass.name}", node)
            return null
        }
        return value.simpleName.asString()
    }

    private fun KSAnnotation.intArgument(name: String, node: KSNode): Int? {
        val value = argumentValue(name) ?: return null
        if (value !is Int) {
            logger.error("Unexpected shape for int argument '$name': ${value.javaClass.name}", node)
            return null
        }
        return value
    }

    private fun KSAnnotation.boolArgument(name: String, node: KSNode): Boolean? {
        val value = argumentValue(name) ?: return null
        if (value !is Boolean) {
            logger.error("Unexpected shape for boolean argument '$name': ${value.javaClass.name}", node)
            return null
        }
        return value
    }

    private fun KSAnnotation.stringArgument(name: String, node: KSNode): String? {
        val value = argumentValue(name) ?: return null
        if (value !is String) {
            logger.error("Unexpected shape for string argument '$name': ${value.javaClass.name}", node)
            return null
        }
        return value
    }
}

private fun KSAnnotated.findAnnotation(fqn: String): KSAnnotation? {
    return annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == fqn
    }
}
