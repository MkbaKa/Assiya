package github.mkbaka.assiya.platform.bukkit.internal.command.sub

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.internal.command.AttributeMessage
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.bukkitksp.annotation.CustomField
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.colonel.argument
import pers.neige.colonel.arguments.impl.PlayerArgument
import pers.neige.colonel.literal

object StatusCommand {

    @CustomField("command")
    val command = literal<CommandSender, Unit>("status") {
        argument("target", PlayerArgument()) {
            setNullExecutor { context ->
                val sender = context.source ?: return@setNullExecutor
                val target = context.getArgument<Player>("target")
                val attributes = AssiyaAPI.getSnapshot(target).attributes
                val variables = mapOf("player" to target.name, "count" to attributes.size.toString())
                MessageType.STATUS_HEADER.sendTo(sender, variables)
                if (attributes.isEmpty()) MessageType.STATUS_EMPTY.sendTo(sender, variables)
                AttributeMessage.send(sender, attributes, variables)
                MessageType.STATUS_FOOTER.sendTo(sender)
            }
        }
    }

}
