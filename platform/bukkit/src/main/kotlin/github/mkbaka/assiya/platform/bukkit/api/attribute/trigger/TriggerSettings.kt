package github.mkbaka.assiya.platform.bukkit.api.attribute.trigger

import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode

class TriggerSettings private constructor(
    private val node: ConfigNode?,
    private val parent: TriggerSettings?,
) {

    val priority: Int
        get() = get<Int>("priority") ?: 0

    val alwaysRun: Boolean
        get() = get<Boolean>("always-run") ?: false

    val handler: String?
        get() = get("handler")

    val periodTicks: Long?
        get() = get<Long>("period")

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? {
        return node?.get(key) as? T ?: parent?.get(key)
    }

    fun child(node: ConfigNode?): TriggerSettings {
        return TriggerSettings(node, this)
    }

    companion object {

        fun root(node: ConfigNode): TriggerSettings {
            return TriggerSettings(node, null)
        }
    }

}
