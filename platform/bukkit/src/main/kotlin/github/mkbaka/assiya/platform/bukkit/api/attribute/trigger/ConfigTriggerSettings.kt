package github.mkbaka.assiya.platform.bukkit.api.attribute.trigger

import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode

class ConfigTriggerSettings private constructor(
    private val node: ConfigNode?,
    private val parent: TriggerSettings?,
) : TriggerSettings {

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(key: String): T? {
        return node?.get(key) as? T ?: parent?.get(key)
    }

    fun child(node: ConfigNode?): ConfigTriggerSettings {
        return ConfigTriggerSettings(node, this)
    }

    companion object {

        fun root(node: ConfigNode): ConfigTriggerSettings {
            return ConfigTriggerSettings(node, null)
        }

    }

}
