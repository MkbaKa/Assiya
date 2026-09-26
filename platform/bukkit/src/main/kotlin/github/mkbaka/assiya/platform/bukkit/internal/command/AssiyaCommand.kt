package github.mkbaka.assiya.platform.bukkit.internal.command

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.CustomFields
import github.mkbaka.assiya.platform.bukkit.internal.util.CommandUtils

import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import pers.neige.colonel.CommandProcessor
import pers.neige.colonel.node.Node
import pers.neige.colonel.root

internal object AssiyaCommand {

    private const val NAME = "assiya"
    private const val PERMISSION = "assiya.command"

    private val node = root<CommandSender, Unit>(NAME)

    private fun create(): PluginCommand {
        val command = CommandUtils.newPluginCommand(NAME, AssiyaBukkitPlugin.INSTANCE)
        command.permission = PERMISSION
        CommandProcessor.processCommand(command, node) { context ->
            val sender = context.source ?: return@processCommand
            sender.sendMessage("TODO usage")
//            println("unknown command")
//            MessageType.USAGE.sendTo(sender)
        }
        return command
    }

    @Awake(lifeCycle = LifeCycle.ENABLE)
    fun register() {
        for (child in CustomFields.get<Node<CommandSender, Unit>>("command")) {
            node.then(child, false)
        }
        node.buildLiteralSearcher()

        CommandUtils.registerCommand(NAME, create())
    }

}
