package github.mkbaka.assiya.platform.bukkit.internal.message.template

import org.bukkit.command.CommandSender

interface Message {

    fun asContent(variables: Map<String, String>): String

    fun sendTo(sender: CommandSender, variables: Map<String, String>)

}

