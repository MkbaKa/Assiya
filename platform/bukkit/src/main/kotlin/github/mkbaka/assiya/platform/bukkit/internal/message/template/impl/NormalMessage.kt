package github.mkbaka.assiya.platform.bukkit.internal.message.template.impl

import github.mkbaka.assiya.platform.bukkit.internal.message.template.Message

import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.colored
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.replaceVariables
import org.bukkit.command.CommandSender

class NormalMessage(private val lines: List<String>) : Message {

    override fun asContent(variables: Map<String, String>): String =
        lines.joinToString("\n") { it.replaceVariables(variables).colored() }

    override fun sendTo(sender: CommandSender, variables: Map<String, String>) {
        for (line in lines) sender.sendMessage(line.replaceVariables(variables).colored())
    }

}
