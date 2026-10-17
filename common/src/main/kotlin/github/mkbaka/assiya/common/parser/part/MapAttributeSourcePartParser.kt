package github.mkbaka.assiya.common.parser.part

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.condition.ConditionContext
import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.operation.Operation
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.Rule
import github.mkbaka.assiya.common.parser.RuleSet
import github.mkbaka.assiya.common.source.AttributeValue

class MapAttributeSourcePartParser(
    rules: RuleSet
) : AttributeSourcePartParser {

    private val attributes = indexById(rules.attributes)
    private val conditions = indexById(rules.conditions)

    override val type: String = MapAttributeSourcePart.TYPE

    private fun indexById(rules: List<Rule>): Map<String, Rule> {
        val indexed = linkedMapOf<String, Rule>()
        for (rule in rules) {
            indexed[rule.id.lowercase()] = rule
        }
        return indexed
    }

    override fun parse(part: AttributeSourcePart): AttributeParsedSource {
        require(part is MapAttributeSourcePart) {
            "非 Map 类型无法由 ${this::class.java.simpleName} 解析"
        }

        val attributes = readAttributes(part.attributes)
        val conditions = readConditions(part.conditions)
        val parsed = AttributeParsedSource.Part(attributes, conditions)
        return AttributeParsedSource(listOf(parsed))
    }

    private fun readAttributes(groups: List<MapAttributeSourcePart.AttributeGroup>): Map<String, AttributeValue<*>> {
        val grouped = linkedMapOf<String, AttributeValue<*>>()
        for (group in groups) {
            for (attribute in group.attributes) {
                val rule = attributes[attribute.id.lowercase()] ?: continue

                val values = readValues(attribute.captures, rule.capture)
                grouped["${group.id}.${attribute.id}"] = AttributeValue(
                    attributeId = rule.id,
                    status = Status.of(values),
                    capture = rule.capture
                )
            }
        }

        return grouped
    }

    private fun readConditions(groups: List<MapAttributeSourcePart.ConditionGroup>): List<AttributeParsedSource.Condition> {
        val results = arrayListOf<AttributeParsedSource.Condition>()
        for (group in groups) {
            val parsed = arrayListOf<ParsedCondition>()
            for (data in group.conditions) {
                parsed += readCondition(data) ?: continue
            }

            results += AttributeParsedSource.Condition(parsed, group.paths)
        }

        return results
    }

    private fun readCondition(data: MapAttributeSourcePart.Condition): ParsedCondition? {
        val condition = conditions[data.id.lowercase()] ?: return null

        // 条件参数
        val parameters = linkedMapOf<String, Any>()
        for ((key, value) in data.parameters) {
            if (value != null) parameters[key] = value
        }
        parameters.putAll(readValues(data.parameters, condition.capture))

        return ParsedCondition(condition.id, ConditionContext(parameters))
    }

    private fun readValues(compound: Map<String, Any?>, capture: Capture<Any>): Map<String, Any> {
        val values = linkedMapOf<String, Any>()
        for ((key, tag) in compound) {
            val entry = capture[key] ?: continue
            val operation: Operation<*> = entry.operation

            val value = when {
                tag is Number && operation is NumberOperations -> tag.toDouble()

                else -> entry.value.parse(tag.toString())
            }

            require(value != null) { "无法读取 capture $key 的值: $tag" }
            values[key] = value
        }
        return values
    }

}
