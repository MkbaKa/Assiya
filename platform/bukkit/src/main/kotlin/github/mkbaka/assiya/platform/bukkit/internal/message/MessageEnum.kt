package github.mkbaka.assiya.platform.bukkit.internal.message

import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import org.bukkit.command.CommandSender
import java.util.function.Consumer

interface MessageEnum {

    val lang: String

    fun asContent(variables: Map<String, String> = emptyMap()): String =
        Config.messages[lang].asContent(mapOf("prefix" to AssiyaBukkitPlugin.PREFIX) + variables)

    fun asContent(variables: Consumer<MutableMap<String, String>>): String =
        asContent(hashMapOf<String, String>().also(variables::accept))

    fun sendTo(sender: CommandSender, variables: Map<String, String> = emptyMap()) {
        Config.messages[lang].sendTo(sender, mapOf("prefix" to AssiyaBukkitPlugin.PREFIX) + variables)
    }

    fun sendTo(sender: CommandSender, variables: Consumer<MutableMap<String, String>>) {
        sendTo(sender, hashMapOf<String, String>().also(variables::accept))
    }

}
