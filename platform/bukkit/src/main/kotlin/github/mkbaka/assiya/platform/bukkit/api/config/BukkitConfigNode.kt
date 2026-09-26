package github.mkbaka.assiya.platform.bukkit.api.config

import org.bukkit.configuration.ConfigurationSection

class BukkitConfigNode(val handle: ConfigurationSection) : ConfigNode {

    override fun keys(): Set<String> = handle.getKeys(false)

    override fun get(key: String): Any? = handle.get(key)

}