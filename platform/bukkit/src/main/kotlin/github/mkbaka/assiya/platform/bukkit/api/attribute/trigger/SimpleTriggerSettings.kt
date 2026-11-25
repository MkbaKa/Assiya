package github.mkbaka.assiya.platform.bukkit.api.attribute.trigger

class SimpleTriggerSettings @JvmOverloads constructor(
    override val priority: Int = 0,
    override val alwaysRun: Boolean = false,
    override val ignoreCancelled: Boolean = false,
    override val handler: String? = null,
    override val periodTicks: Long? = null,
    options: Map<String, Any?> = emptyMap(),
) : TriggerSettings {

    private val options = options.toMap(HashMap())

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(key: String): T? {
        return options[key] as? T
    }

}
