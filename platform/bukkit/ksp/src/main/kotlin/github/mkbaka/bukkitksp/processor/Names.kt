package github.mkbaka.bukkitksp.processor

internal const val ANNOTATION_PACKAGE = "github.mkbaka.bukkitksp.annotation"

internal const val LISTENER_FQN = "$ANNOTATION_PACKAGE.Listener"
internal const val AWAKE_FQN = "$ANNOTATION_PACKAGE.Awake"
internal const val CUSTOM_TASK_FQN = "$ANNOTATION_PACKAGE.CustomTask"
internal const val CUSTOM_FIELD_FQN = "$ANNOTATION_PACKAGE.CustomField"

internal const val JAVA_PLUGIN_FQN = "org.bukkit.plugin.java.JavaPlugin"

internal const val BUKKIT_LISTENERS_NAME = "BukkitListeners"
internal const val AWAKE_LIFECYCLE_NAME = "AwakeLifecycle"
internal const val CUSTOM_TASKS_NAME = "CustomTasks"
internal const val CUSTOM_FIELDS_NAME = "CustomFields"

internal const val OPTION_PREFIX = "bukkitksp."
internal const val GENERATED_PACKAGE_OPTION = "${OPTION_PREFIX}generatedPackage"
internal const val PLUGIN_INSTANCE_OPTION = "${OPTION_PREFIX}pluginInstance"
internal val KNOWN_OPTIONS = setOf(GENERATED_PACKAGE_OPTION, PLUGIN_INSTANCE_OPTION)

internal const val LISTENER_REGISTER_PRIORITY = -1000
internal const val LISTENER_UNREGISTER_PRIORITY = 1000
