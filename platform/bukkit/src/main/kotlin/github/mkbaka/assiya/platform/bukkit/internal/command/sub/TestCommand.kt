package github.mkbaka.assiya.platform.bukkit.internal.command.sub

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.command.argument.ScriptEngineArgument
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.bukkitksp.annotation.CustomField
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.colonel.argument
import pers.neige.colonel.arguments.impl.StringArgument
import pers.neige.colonel.literal

object TestCommand {

    @CustomField("command")
    val command = literal<CommandSender, Unit>("test") {
        literal("evalScript") {
            argument("type", ScriptEngineArgument()) {
                argument("content", StringArgument(1, Int.MAX_VALUE, true)) {
                    setNullExecutor { context ->
                        val sender = context.source ?: return@setNullExecutor
                        val engine = context.getArgument<ScriptEngineAdapter>("type")
                        val content = context.getArgument<String>("content")
                        engine.eval(
                            content,
                            hashMapOf(
                                "sender" to sender,
                                "player" to (sender as? Player),
                                "plugin" to AssiyaBukkitPlugin.INSTANCE
                            )
                        )
                    }
                }
            }
        }
    }

}
