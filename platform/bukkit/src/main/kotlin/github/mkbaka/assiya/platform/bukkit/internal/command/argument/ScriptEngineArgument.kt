package github.mkbaka.assiya.platform.bukkit.internal.command.argument

import github.mkbaka.assiya.platform.bukkit.internal.config.ScriptManager
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import org.bukkit.command.CommandSender
import pers.neige.colonel.arguments.Argument
import pers.neige.colonel.arguments.ParseResult
import pers.neige.colonel.context.Context
import pers.neige.colonel.context.NodeChain
import pers.neige.colonel.reader.StringReader

class ScriptEngineArgument : Argument<CommandSender, ScriptEngineAdapter, Unit>() {

    override fun parse(
        chain: NodeChain<CommandSender, Unit>,
        input: StringReader,
        source: CommandSender?
    ): ParseResult<ScriptEngineAdapter> {
        val start = input.offset
        val engine = ScriptManager.getEngine(input.readString())
        if (engine == null) {
            input.offset = start
            return ParseResult(null, false)
        }
        return ParseResult(engine, true)
    }

    override fun tab(context: Context<CommandSender, Unit>, remaining: String): List<String> =
        ScriptManager.engineTypes.filter { it.startsWith(remaining, ignoreCase = true) }

}
