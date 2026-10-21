package github.mkbaka.assiya.platform.bukkit.internal.command.sub

import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
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
                var success = false
                val elapsed = measureTime { success = Config.reload() }
                if (success) MessageType.RELOAD_SUCCESS.sendTo(
                    sender,
                    mapOf("time" to elapsed.inWholeMilliseconds.toString())
                )
                else MessageType.RELOAD_UNAVAILABLE.sendTo(sender)
            }
        }
    }

}
