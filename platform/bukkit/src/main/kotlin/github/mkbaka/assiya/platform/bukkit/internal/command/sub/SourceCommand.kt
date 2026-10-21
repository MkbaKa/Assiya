package github.mkbaka.assiya.platform.bukkit.internal.command.sub

import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.internal.command.AttributeMessage
import github.mkbaka.assiya.platform.bukkit.internal.command.argument.SourceArgument
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.bukkitksp.annotation.CustomField
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.colonel.argument
import pers.neige.colonel.arguments.impl.BooleanArgument
import pers.neige.colonel.arguments.impl.IntegerArgument
import pers.neige.colonel.arguments.impl.LongArgument
import pers.neige.colonel.arguments.impl.PlayerArgument
import pers.neige.colonel.arguments.impl.StringArgument
import pers.neige.colonel.literal
import kotlin.time.Duration.Companion.seconds

object SourceCommand {

    @CustomField("command")
    val command = literal<CommandSender, Unit>("source") {
        literal("list") {
            argument("player", PlayerArgument()) {
                setNullExecutor { context ->
                    val sender = context.source ?: return@setNullExecutor
                    list(sender, context.getArgument("player"))
                }
            }
        }

        literal("info") {
            argument("player", PlayerArgument()) {
                argument("source", SourceArgument("player")) {
                    setNullExecutor { context ->
                        val sender = context.source ?: return@setNullExecutor
                        info(sender, context.getArgument("player"), context.getArgument("source"))
                    }
                }
            }
        }

        literal("give") {
            argument("player", PlayerArgument()) {
                argument("source", StringArgument()) {
                    argument(
                        "priority",
                        IntegerArgument<CommandSender, Unit>().apply {
                            setNullFailExecutor { context ->
                                val sender = context.source ?: return@setNullFailExecutor
                                MessageType.INVALID_PRIORITY.sendTo(sender)
                            }
                        }
                    ) {
                        argument("attributes", StringArgument()) {
                            argument(
                                "duration",
                                LongArgument<CommandSender, Unit>().apply {
                                    setDefaultValue(-1)
                                    setNullFailExecutor { context ->
                                        val sender = context.source ?: return@setNullFailExecutor
                                        MessageType.INVALID_DURATION.sendTo(sender)
                                    }
                                }
                            ) {
                                argument(
                                    "silent",
                                    BooleanArgument<CommandSender, Unit>().apply { setDefaultValue(false) }
                                ) {
                                    setNullExecutor { context ->
                                        val sender = context.source ?: return@setNullExecutor
                                        give(
                                            sender,
                                            context.getArgument("player"),
                                            context.getArgument("source"),
                                            context.getArgument("priority"),
                                            context.getArgument("attributes"),
                                            context.getArgument("duration"),
                                            context.getArgument("silent")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        literal("remove") {
            argument("player", PlayerArgument()) {
                argument("source", SourceArgument("player")) {
                    argument(
                        "silent",
                        BooleanArgument<CommandSender, Unit>().apply { setDefaultValue(false) }
                    ) {
                        setNullExecutor { context ->
                            val sender = context.source ?: return@setNullExecutor
                            remove(
                                sender,
                                context.getArgument("player"),
                                context.getArgument("source"),
                                context.getArgument("silent")
                            )
                        }
                    }
                }
            }
        }
    }

    private fun list(sender: CommandSender, player: Player) {
        val sources = AssiyaAPI.getSnapshot(player).sources
        MessageType.SOURCE_LIST_HEADER.sendTo(
            sender,
            mapOf("player" to player.name, "count" to sources.size.toString())
        )
        for (source in sources.keys) {
            MessageType.SOURCE_LIST_ENTRY.sendTo(sender, mapOf("player" to player.name, "source" to source))
        }
        MessageType.SOURCE_LIST_FOOTER.sendTo(sender)
    }

    private fun info(sender: CommandSender, player: Player, source: String) {
        val vars = mapOf("player" to player.name, "source" to source)
        val record = AssiyaAPI.getSnapshot(player).sources[source]
        if (record == null) {
            MessageType.SOURCE_ABSENT.sendTo(sender, vars)
            return
        }
        MessageType.SOURCE_INFO_HEADER.sendTo(sender, vars + ("count" to record.attributes.size.toString()))
        sendAttributes(sender, record.attributes, vars)
        MessageType.SOURCE_INFO_FOOTER.sendTo(sender)
    }

    private fun give(
        sender: CommandSender,
        player: Player,
        source: String,
        priority: Int,
        attributesText: String,
        duration: Long,
        silent: Boolean
    ) {
        val session = AssiyaAPI.getSession(player)
        if (session == null) {
            MessageType.SOURCE_DATA_NOT_LOADED.sendTo(sender, mapOf("player" to player.name))
            return
        }

        val lifeCycle = when {
            duration > 0 -> AttributeSourceLifecycle.Temporary(duration.seconds)

            else -> AttributeSourceLifecycle.Stable
        }

        val attributes = AssiyaAPI.parse(player, attributesText.split(','))

        if (attributes.isEmpty()) {
            MessageType.SOURCE_EMPTY.sendTo(sender)
            return
        }

        AssiyaAPI.addAttribute(
            session,
            source,
            attributes,
            priority,
            lifeCycle
        )

        if (silent) return

        if (lifeCycle == AttributeSourceLifecycle.Stable) {
            MessageType.SOURCE_GIVE_SUBMITTED_STABLE.sendTo(
                sender, mapOf(
                    "player" to player.name,
                    "source" to source,
                    "count" to attributes.size.toString()
                )
            )
        } else {
            MessageType.SOURCE_GIVE_SUBMITTED.sendTo(
                sender, mapOf(
                    "player" to player.name,
                    "source" to source,
                    "duration" to duration.toString(),
                    "count" to attributes.size.toString()
                )
            )
        }
        sendAttributes(sender, attributes, mapOf("player" to player.name, "source" to source))
        MessageType.SOURCE_INFO_FOOTER.sendTo(sender)
    }

    private fun remove(sender: CommandSender, player: Player, source: String, silent: Boolean) {
        val vars = mapOf("player" to player.name, "source" to source)
        if (source !in AssiyaAPI.getSnapshot(player).sources) {
            MessageType.SOURCE_ABSENT.sendTo(sender, vars)
            return
        }
        AssiyaAPI.takeAttribute(player, source)
        if (!silent) MessageType.SOURCE_REMOVE_SUBMITTED.sendTo(sender, vars)
    }

    private fun sendAttributes(
        sender: CommandSender,
        attributes: Map<String, AttributeValue<*>>,
        variables: Map<String, String>
    ) {
        AttributeMessage.send(sender, attributes.mapValues { it.value.status }, variables)
    }

}
