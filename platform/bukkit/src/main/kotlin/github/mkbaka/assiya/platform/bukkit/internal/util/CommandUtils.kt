@file:Suppress("DEPRECATION", "removal")

package github.mkbaka.assiya.platform.bukkit.internal.util

import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager

object CommandUtils {

    private val isPaper by lazy {
        hasClass("com.destroystokyo.paper.PaperConfig")
            || hasClass("io.papermc.paper.configuration.Configuration")
    }

    private val pluginCommandConstructor by lazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java)
            .apply { isAccessible = true }
    }

    private val commandMap by lazy {
        if (isPaper) {
            Bukkit.getServer().commandMap
        } else {
            val field = SimplePluginManager::class.java.getDeclaredField("commandMap")
            field.isAccessible = true
            field.get(Bukkit.getPluginManager()) as CommandMap
        }
    }

    fun newPluginCommand(name: String, owner: Plugin): PluginCommand {
        return pluginCommandConstructor.newInstance(name, owner)
    }

    fun registerCommand(fallbackPrefix: String, command: PluginCommand) {
        commandMap.register(fallbackPrefix, command)
    }

}