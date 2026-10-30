package github.mkbaka.assiya.compact.mythicmobs.v4.util

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.base.Value
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.formula.FormulaCompiler
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeSnapshotBuilder
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString
import org.bukkit.entity.Entity
import java.math.BigDecimal

object Utils {

    fun parseAttributes(
        entity: Entity,
        variables: Map<String, String>
    ): Map<String, Status<Any>> {
        // 未指定 capture id 的属性行
        // 由于未指定 无法得知 value 被哪行正则匹配
        // 所以拼成一坨文本解析后再与指定 capture id 的结果合并
        val lines = arrayListOf<String>()
        val captures = hashMapOf<String, String>()

        for ((name, value) in variables) {
            // 带 | 说明是指定 "attribute id | capture id" 的组合
            // 如 "attack|min"
            // 反之默认为 "属性名" = "属性值"
            if ('|' in name) {
                captures[name] = value
            } else {
                val attribute = AttributeRegistry.findByName(name)
                // 自定义参数不参与公式计算, 仍按原样保留.
                val text = if (attribute == null) value else calculate(name, value)
                lines += "$name $text"
            }
        }

        // 解析后的属性结果
        val attributes = hashMapOf<String, Status<Any>>()
        // 解析后条件通过才可用
        if (lines.isNotEmpty()) {
            val parsed = AssiyaAPI.parse(entity, lines)

            for ((attributeId, attribute) in parsed) {
                @Suppress("UNCHECKED_CAST")
                attributes[attributeId] = attribute.status as Status<Any>
            }
        }

        for ((name, raw) in captures) {
            // "attack|min"  ->  "attack" , "min"
            val attributeId = name.substringBefore("|")
            val captureId = name.substringAfter("|")

            // TODO 待优化
            val attribute = AttributeRegistry[attributeId] ?: error("Unknown attribute: $attributeId")
            val reader = attribute[ReaderComponent]?.reader ?: error("$attributeId has no reader")
            val capture = reader.capture[captureId] ?: error("$attributeId#${reader.id} not has capture: $captureId")
            val text = if (capture.value == Value.Number) calculate(name, raw) else raw
            val value = capture.value.parse(text) ?: error("$attributeId#${reader.id} parsed invalid value: $raw")

            val previous = attributes[attributeId]
            val values = hashMapOf<String, Any>()

            if (previous != null) {
                for ((id, previousValue) in previous.entries) values[id] = previousValue
            }
            values[captureId] = value
            attributes[attribute.id] = Status.of(values)
        }

        return attributes
    }

    private fun calculate(name: String, expression: String): String {
        val formula = FormulaCompiler.compile("damage-meta.$name", expression)
        val result = formula.invoke { null }
        return BigDecimal.valueOf(result).stripTrailingZeros().toPlainString()
    }

    fun parseAllEntries(
        variables: Map<String, PlaceholderString>,
        meta: SkillMetadata,
        target: AbstractEntity
    ): MutableMap<String, String> {
        return variables.asSequence().map {
            val value = it.value.get(meta, target)
            it.key to value
        }.toMap(HashMap())
    }

    fun resolveAttributeIds(entries: List<String>?): Set<String> {
        if (entries.isNullOrEmpty()) return emptySet()

        val ids = hashSetOf<String>()
        for (entry in entries) {
            val attribute = AttributeRegistry[entry] ?: AttributeRegistry.findByName(entry) ?: continue
            ids += attribute.id
        }
        return ids
    }

    fun selectDamageSubscribers(usableAttribute: List<String>?): List<HandlerRegistry.Subscriber>? {
        if (usableAttribute == null) return null

        val usableIds = resolveAttributeIds(usableAttribute)
        return HandlerRegistry.select("damage") { subscriber ->
            subscriber.attributeId in usableIds
        }
    }

    fun buildDamageAttackerSnapshot(
        attacker: Entity,
        parsed: Map<String, String>,
        clear: Boolean,
        overwrite: Boolean
    ) = AttributeSnapshotBuilder(
        if (clear) AttributeEngineSnapshot.EMPTY else AssiyaAPI.getSnapshot(attacker)
    ).also { builder ->
        for ((attributeId, status) in parseAttributes(attacker, parsed)) {
            if (overwrite) builder.setAttribute(attributeId, status) else builder.addAttribute(attributeId, status)
        }
    }.build()

    fun disableAttributes(builder: ProcessorComponentBuilder, disabledIds: Set<String>) {
        for (id in disabledIds) {
            builder[CancellableAttributeComponent]!!.setCancelled(id)
        }
    }

}
