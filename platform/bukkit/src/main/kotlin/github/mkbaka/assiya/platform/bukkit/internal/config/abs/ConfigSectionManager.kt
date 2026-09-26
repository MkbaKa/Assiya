package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode


abstract class ConfigSectionManager<T : Any>(
    configPath: String
) : AbstractConfigManager<ConfigNode, T>(configPath) {

    override fun readConfig(node: ConfigNode, key: String): ConfigNode? = node.getNode(key)

}
