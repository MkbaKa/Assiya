package github.mkbaka.assiya.platform.bukkit.internal.command.argument

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.colonel.arguments.Argument
import pers.neige.colonel.arguments.ParseResult
import pers.neige.colonel.context.Context
import pers.neige.colonel.context.NodeChain
import pers.neige.colonel.reader.StringReader

class SourceArgument(private val playerArgument: String) : Argument<CommandSender, String, Unit>() {

    override fun parse(
        chain: NodeChain<CommandSender, Unit>,
        input: StringReader,
        source: CommandSender?
    ): ParseResult<String> =
        ParseResult(input.readString(), true)

    override fun tab(context: Context<CommandSender, Unit>, remaining: String): List<String> {
        val player = context.nodeChain.getArgument<Player>(playerArgument) ?: return emptyList()

        val remaining = remaining.lowercase()
        return AssiyaAPI.getSnapshot(player).sources.keys
            .filter { it.lowercase().startsWith(remaining) }
    }

}
