package github.mkbaka.assiya.platform.bukkit.internal.util

import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager

object CommandUtils {

    private val pluginCommandConstructor by lazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java)
            .apply { isAccessible = true }
    }

    /** Paper 公开了 Server#getCommandMap (1.12.2 起可用), 其余服务端反射 SimplePluginManager. */
    private val commandMap by lazy {
        if (isPaper) {
            Bukkit.getServer().commandMap
        } else {
            val field = SimplePluginManager::class.java.getDeclaredField("commandMap")
            field.isAccessible = true
            field.get(Bukkit.getPluginManager()) as CommandMap
        }
    }

    /** 反射构造 PluginCommand — Bukkit 不公开其构造器, 运行期注册命令的唯一入口. */
    fun newPluginCommand(name: String, owner: Plugin): PluginCommand {
        return pluginCommandConstructor.newInstance(name, owner)
    }

    /** 注册进服务端命令表; fallbackPrefix 用于命令名冲突时的 'prefix:name' 兜底别名. */
    fun registerCommand(fallbackPrefix: String, command: PluginCommand) {
        commandMap.register(fallbackPrefix, command)
    }

}