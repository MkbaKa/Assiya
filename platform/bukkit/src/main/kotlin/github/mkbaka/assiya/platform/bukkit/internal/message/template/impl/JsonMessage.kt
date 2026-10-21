package github.mkbaka.assiya.platform.bukkit.internal.message.template.impl

import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.message.template.Message
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.colored
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.replaceVariables
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

class JsonMessage(node: ConfigNode) : Message {

    private class Part(
        val text: String,
        val action: ClickEvent.Action?,
        val value: String?,
        val hover: String?
    )

    private val parts: List<Part> = buildList {
        val section = requireNotNull(node.getNode("parts")) { "JSON message is missing parts" }
        for (key in section.keys()) {
            val part = requireNotNull(section.getNode(key))
            val action = when (part.getString("action")) {
                "command" -> ClickEvent.Action.RUN_COMMAND
                "suggest" -> ClickEvent.Action.SUGGEST_COMMAND
                null -> null
                else -> error("Unknown message click action: ${part.getString("action")}")
            }
            add(
                Part(
                    part.getStrings("content").joinToString("\n"),
                    action,
                    part.getString("value"),
                    part.getStrings("hover").takeIf { it.isNotEmpty() }?.joinToString("\n")
                )
            )
        }
    }

    override fun asContent(variables: Map<String, String>): String =
        parts.joinToString("") { it.text.replaceVariables(variables).colored() }

    override fun sendTo(sender: CommandSender, variables: Map<String, String>) {
        val components = arrayListOf<BaseComponent>()
        for (part in parts) {
            val click = part.action?.let {
                ClickEvent(it, requireNotNull(part.value).replaceVariables(variables))
            }
            val hover = part.hover?.let {
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(
                        it.replaceVariables(variables).colored().lineSequence().joinToString("\n") { line -> "$line   " }
                    )
                )
            }
            for (component in TextComponent.fromLegacyText(part.text.replaceVariables(variables).colored())) {
                component.clickEvent = click
                component.hoverEvent = hover
                components += component
            }
        }
        sender.spigot().sendMessage(*components.toTypedArray())
    }

}
