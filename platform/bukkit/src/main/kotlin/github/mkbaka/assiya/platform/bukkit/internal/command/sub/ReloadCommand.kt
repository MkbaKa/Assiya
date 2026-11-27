package github.mkbaka.assiya.platform.bukkit.internal.command.sub

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.command.argument.ReloadModuleArgument
import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadModule
import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadResult
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.bukkitksp.annotation.CustomField
import org.bukkit.command.CommandSender
import pers.neige.colonel.argument
import pers.neige.colonel.literal
import kotlin.time.measureTime

object ReloadCommand {

    @CustomField("command")
    val command = literal<CommandSender, Unit>("reload") {
        argument("module", ReloadModuleArgument()) {
            setNullExecutor { context ->
                val sender = context.source ?: return@setNullExecutor
                val module = context.getArgument<ReloadModule>("module")

                BukkitCoroutine.launch {
                    var result: ReloadResult
                    val elapsed = measureTime {
                        result = Config.reloadModule(module)
                    }

                    when {
                        result == ReloadResult.BUSY -> {
                            MessageType.RELOAD_UNAVAILABLE.sendTo(sender)
                        }

                        result == ReloadResult.FAILED -> {
                            MessageType.RELOAD_FAILED.sendTo(sender)
                        }

                        module == ReloadModule.ALL -> {
                            MessageType.RELOAD_SUCCESS.sendTo(
                                sender,
                                mapOf(
                                    "name" to AssiyaBukkitPlugin.PLUGIN_NAME,
                                    "time" to elapsed.inWholeMilliseconds.toString()
                                )
                            )
                        }

                        else -> {
                            MessageType.RELOAD_MODULE_SUCCESS.sendTo(
                                sender,
                                mapOf(
                                    "name" to AssiyaBukkitPlugin.PLUGIN_NAME,
                                    "module" to module.id,
                                    "time" to elapsed.inWholeMilliseconds.toString()
                                )
                            )
                        }
                    }


                }
            }
        }
    }

}
