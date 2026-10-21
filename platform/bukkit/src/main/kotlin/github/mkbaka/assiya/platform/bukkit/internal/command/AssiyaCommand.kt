package github.mkbaka.assiya.platform.bukkit.internal.command

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.CustomFields
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.assiya.platform.bukkit.internal.util.CommandUtils

import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import pers.neige.colonel.CommandProcessor
import pers.neige.colonel.node.Node
import pers.neige.colonel.root

object AssiyaCommand {

    private const val NAME = "assiya"

    private val node = root<CommandSender, Unit>(NAME)

    private fun create(): PluginCommand {
        val command = CommandUtils.newPluginCommand(NAME, AssiyaBukkitPlugin.INSTANCE)

        CommandProcessor.processCommand(
            command,
            node,
            { context, _ ->
                val sender = context.source ?: return@processCommand
                MessageType.NO_PERMISSION.sendTo(sender)
            },
            { context ->
                val sender = context.source ?: return@processCommand
                MessageType.USAGE.sendTo(sender, mapOf("version" to AssiyaBukkitPlugin.INSTANCE.description.version))
            }
        )
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
