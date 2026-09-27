package github.mkbaka.assiya.platform.bukkit.internal.command.sub

import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.callAsync
import github.mkbaka.bukkitksp.annotation.CustomField
import org.bukkit.command.CommandSender
import pers.neige.colonel.literal
import kotlin.time.measureTime

object ReloadCommand {

    @CustomField("command")
    val command = literal<CommandSender, Unit>("reload") {
        setNullExecutor { context ->
            val sender = context.source ?: return@setNullExecutor

            callAsync {
                measureTime {
                    if (!Config.reload()) {
//                    MessageType.RELOAD_IN_PROGRESS.sendTo(sender)
                        return@callAsync
                    }
                }.also { used ->
                    sender.sendMessage("reload used ${used.inWholeMilliseconds} ms.")
                }

//                MessageType.RELOAD_SUCCESS.sendTo(sender) { vars ->
//                    vars["scripts"] = ScriptManager.size.toString()
//                    vars["readers"] = ReaderRegistry.values.size.toString()
////                    vars["placeholders"] = FormulaManager.values.size.toString()
//                    vars["conditions"] = ConditionRegistry.values.size.toString()
//                    vars["attributes"] = AttributeRegistry.values.size.toString()
//                }
            }
        }
    }

}
