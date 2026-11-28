package github.mkbaka.assiya.common.parser

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.common.source.mergeAttributeValues
import java.util.function.Predicate

class AttributeParsedSource(val parts: List<Part>) {

    class Part(val attributes: Map<String, AttributeValue<*>>, val conditions: List<Condition>)

    class Condition(
        val conditions: List<ParsedCondition>,
        val paths: List<String> = emptyList()
    )

    fun evaluateConditions(evaluator: Predicate<ParsedCondition>): BooleanArray {
        val results = BooleanArray(parts.sumOf { it.conditions.size })
        var index = 0
        for (part in parts) {
            for (condition in part.conditions) {
                val passed = condition.conditions.all(evaluator::test)
                results[index++] = passed
                if (!passed && condition.paths.isEmpty()) return results
            }
        }
        return results
    }

    fun resolve(evaluator: Predicate<ParsedCondition>): Map<String, AttributeValue<*>> {
        val conditions = evaluateConditions(evaluator)
        return resolve(conditions)
    }

    /**
     * 忽略条件限制
     */
    fun resolve(): Map<String, AttributeValue<*>> {
        return resolve(BooleanArray(parts.sumOf { it.conditions.size }) { true })
    }

    fun resolve(conditions: BooleanArray): Map<String, AttributeValue<*>> {
        val accepted = arrayListOf<AttributeValue<*>>()
        var index = 0
        for (part in parts) {
            var attributes: MutableMap<String, AttributeValue<*>>? = null

            for (condition in part.conditions) {
                // 条件满足
                if (conditions[index++]) continue
                // 不指定路径就是整个来源级的条件
                if (condition.paths.isEmpty()) return emptyMap()

                // 解析结果可能会被多个实体、来源复用 所以不能直接改原来的属性列表
                // 有条件不满足出现删除需求时才应该搓个新的列表去修改
                if (attributes == null) attributes = part.attributes.toMutableMap()
                for (path in condition.paths) removePath(attributes, path)
            }

            accepted.addAll((attributes ?: part.attributes).values)
        }

        return mergeAttributeValues(accepted)
    }

    private fun removePath(attributes: MutableMap<String, AttributeValue<*>>, path: String) {
        val iterator = attributes.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key == path || entry.key.startsWith("$path.")) {
                // 路径指向属性本身或它的分组 整条属性一起删
                iterator.remove()
            } else if (path.startsWith("${entry.key}.")) {
                // 路径比属性多出一层 那就只删这个 capture
                val value = withoutCapture(entry.value, path.removePrefix("${entry.key}."))
                if (value.status.isEmpty) iterator.remove() else entry.setValue(value)
            }
        }
    }

    private fun <T : Any> withoutCapture(value: AttributeValue<T>, captureId: String): AttributeValue<T> {
        if (value.status[captureId] == null) return value

        val remaining = linkedMapOf<String, T>()
        for ((id, captured) in value.status.entries) {
            if (id != captureId) remaining[id] = captured
        }

        return value.copy(status = Status.of(remaining))
    }

    companion object {
        val EMPTY = AttributeParsedSource(emptyList())
    }
}
