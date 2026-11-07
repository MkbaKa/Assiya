package github.mkbaka.assiya.platform.bukkit.internal.command.argument

import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadModule
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import org.bukkit.command.CommandSender
import pers.neige.colonel.arguments.Argument
import pers.neige.colonel.arguments.ParseResult
import pers.neige.colonel.context.Context
import pers.neige.colonel.context.NodeChain
import pers.neige.colonel.reader.StringReader

class ReloadModuleArgument : Argument<CommandSender, ReloadModule, Unit>() {

    init {
        setDefaultValue(ReloadModule.ALL)
        setNullFailExecutor { context ->
            val sender = context.source ?: return@setNullFailExecutor
            MessageType.RELOAD_UNKNOWN_MODULE.sendTo(sender, mapOf("module" to context.input.readString()))
        }
    }

    override fun parse(
        chain: NodeChain<CommandSender, Unit>,
        input: StringReader,
        source: CommandSender?
    ): ParseResult<ReloadModule> {
        val start = input.offset
        val module = ReloadModule.byId(input.readString())
        if (module == null) {
            input.offset = start
            return ParseResult(null, false)
        }
        return ParseResult(module, true)
    }

    override fun tab(context: Context<CommandSender, Unit>, remaining: String): List<String> =
        ReloadModule.entries.filter { it.id.startsWith(remaining, ignoreCase = true) }.map { it.id }

}
