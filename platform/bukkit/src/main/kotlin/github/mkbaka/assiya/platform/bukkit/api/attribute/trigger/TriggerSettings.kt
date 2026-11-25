package github.mkbaka.assiya.platform.bukkit.api.attribute.trigger

interface TriggerSettings {

    val priority: Int
        get() = get<Int>("priority") ?: 0

    val alwaysRun: Boolean
        get() = get<Boolean>("always-run") ?: false

    val ignoreCancelled: Boolean
        get() = get<Boolean>("ignore-cancelled") ?: false

    val handler: String?
        get() = get("handler")

    val periodTicks: Long?
        get() = get<Number>("period")?.toLong()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T?

}
