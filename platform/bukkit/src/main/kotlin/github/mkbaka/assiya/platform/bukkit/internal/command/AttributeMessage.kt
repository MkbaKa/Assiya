package github.mkbaka.assiya.platform.bukkit.internal.command

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.format
import org.bukkit.command.CommandSender

object AttributeMessage {

    fun send(sender: CommandSender, attributes: Map<String, Status<*>>, context: Map<String, String>) {
        // 用取最长的 id 算需要对齐多少空格
        val longestId = attributes.keys.maxOfOrNull { it.length } ?: 0
        for ((id, status) in attributes) {
            MessageType.SOURCE_INFO_ENTRY.sendTo(sender, context + variables(id, status, longestId - id.length))
        }
    }

    private fun variables(id: String, status: Status<*>, padding: Int): Map<String, String> {
        val component = AttributeRegistry[id]?.get(ReaderComponent)
        val reader = component?.reader
        return mapOf(
            "id" to id,
            "names" to names(component, padding),
            "captures" to captures(status, reader),
            "placeholders" to placeholders(status, reader)
        )
    }

    /**
     * 读取用的名字
     */
    private fun names(component: ReaderComponent?, padding: Int): String {
        if (component == null) return ""
        val text = MessageType.SOURCE_INFO_NAMES.asContent(mapOf("names" to component.names.joinToString(" ")))
        return if (padding > 0) " ".repeat(padding) + text else text
    }

    private fun display(value: Any?): String {
        return if (value is Number) value.format(2) else value.toString()
    }

    /**
     * capture value
     */
    private fun captures(status: Status<*>, reader: Reader?): String {
        val values = arrayListOf<String>()
        val ids = reader?.capture?.ids ?: status.entries.map { it.key }
        for (capture in ids) {
            val captured = status[capture] ?: reader?.defaultValue(capture)
            values += MessageType.SOURCE_INFO_VALUE.asContent(mapOf("id" to capture, "value" to display(captured)))
        }
        return values.ifEmpty { listOf(MessageType.SOURCE_INFO_NONE.asContent()) }.joinToString("\n")
    }

    /**
     * 需要计算的 capture placeholder
     */
    private fun placeholders(status: Status<*>, reader: Reader?): String {
        if (reader == null) return ""
        val evaluation = reader.evaluate(status)
        val placeholderValues = arrayListOf<String>()
        for (placeholderId in reader.placeholders.keys) {
            placeholderValues += MessageType.SOURCE_INFO_VALUE.asContent(mapOf(
                "id" to placeholderId, "value" to display(evaluation.read(placeholderId))
            ))
        }
        return MessageType.SOURCE_INFO_PLACEHOLDERS.asContent(
            mapOf("values" to placeholderValues.ifEmpty {
                listOf(MessageType.SOURCE_INFO_NONE.asContent())
            }.joinToString("\n"))
        )
    }

}
