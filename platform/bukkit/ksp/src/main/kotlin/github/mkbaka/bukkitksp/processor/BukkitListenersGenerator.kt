package github.mkbaka.bukkitksp.processor

fun renderBukkitListeners(generatedPackage: String, pluginInstance: String, listeners: List<ListenerModel>): String {
    val sorted = listeners.sortedBy { it.objectSimpleName }

    val allEventTypes = sorted.flatMap { it.handlers.map { h -> h.eventTypeName } }.distinct()
    val collidingEventTypes = findCollidingSimpleNames(allEventTypes)

    val imports = buildList {
        for (listener in sorted) {
            add(listener.objectName)
            for (handler in listener.handlers) {
                if (handler.eventTypeName.substringAfterLast('.') !in collidingEventTypes) {
                    add(handler.eventTypeName)
                }
            }
        }
        add("org.bukkit.Bukkit")
        add("org.bukkit.event.Event")
        add("org.bukkit.event.EventPriority")
        add("org.bukkit.event.HandlerList")
        add("org.bukkit.event.Listener")
    }.distinct().sorted()

    return buildString {
        appendLine("package $generatedPackage")
        appendLine()
        for (import in imports) {
            appendLine("import $import")
        }
        appendLine()
        appendLine("object $BUKKIT_LISTENERS_NAME {")
        appendLine()
        appendLine("    private val plugin get() = $pluginInstance")
        appendLine()

        for (listener in sorted) {
            val handleName = listener.handleName()
            appendLine("    private val $handleName: Listener = object : Listener {}")
        }
        appendLine()

        appendLine("    fun register() {")
        for (listener in sorted) {
            appendLine("        ${listener.registerHelperName()}()")
        }
        appendLine("    }")
        appendLine()

        appendLine("    fun unregister() {")
        for (listener in sorted) {
            appendLine("        HandlerList.unregisterAll(${listener.handleName()})")
        }
        appendLine("    }")

        for (listener in sorted) {
            appendLine()
            val handleName = listener.handleName()
            appendLine("    private fun ${listener.registerHelperName()}() {")
            for (handler in listener.handlers.sortedBy { it.functionName }) {
                val eventName = resolveCallName(handler.eventTypeName, collidingEventTypes)
                val params = buildList {
                    add("$eventName::class.java")
                    if (handler.priority != "NORMAL") {
                        add("priority = EventPriority.${handler.priority}")
                    }
                    if (handler.ignoreCancelled) {
                        add("ignoreCancelled = true")
                    }
                    add("listener = $handleName")
                }.joinToString(", ")
                appendLine("        on($params) { ${listener.objectSimpleName}.${handler.functionName}(this) }")
            }
            appendLine("    }")
        }

        appendLine()
        appendLine("    @Suppress(\"UNCHECKED_CAST\")")
        appendLine("    private fun <T : Event> on(")
        appendLine("        eventClass: Class<T>,")
        appendLine("        priority: EventPriority = EventPriority.NORMAL,")
        appendLine("        ignoreCancelled: Boolean = false,")
        appendLine("        listener: Listener,")
        appendLine("        executor: T.() -> Unit,")
        appendLine("    ) {")
        appendLine("        Bukkit.getPluginManager()")
        appendLine("            .registerEvent(")
        appendLine("                eventClass, listener, priority, { _, event ->")
        appendLine("                    if (eventClass.isAssignableFrom(event::class.java)) {")
        appendLine("                        executor.invoke(event as T)")
        appendLine("                    }")
        appendLine("                }, plugin, ignoreCancelled")
        appendLine("            )")
        appendLine("    }")

        appendLine()
        appendLine("}")
    }
}

fun validateListenerObjectNames(listeners: List<ListenerModel>): List<String> {
    return listeners
        .groupBy { it.objectSimpleName }
        .filter { (_, group) -> group.size > 1 }
        .map { (simpleName, group) ->
            val fqns = group.joinToString(", ") { it.objectName }
            "Listener objects with the same simple name generate colliding members in $BUKKIT_LISTENERS_NAME: '$simpleName' ($fqns)"
        }
}

private fun ListenerModel.handleName(): String =
    objectSimpleName.replaceFirstChar { it.lowercase() } + "Handle"

private fun ListenerModel.registerHelperName(): String =
    "register${objectSimpleName}"
