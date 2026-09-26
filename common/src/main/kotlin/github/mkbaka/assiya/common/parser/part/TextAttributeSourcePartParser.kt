package github.mkbaka.assiya.common.parser.part

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.condition.ConditionContext
import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.rule.TextPatternTemplate
import github.mkbaka.assiya.common.parser.rule.TextRule
import github.mkbaka.assiya.common.parser.rule.TextRuleMatched
import github.mkbaka.assiya.common.parser.rule.Trie
import github.mkbaka.assiya.common.source.AttributeValue

/**
 * 对文本的解析实现
 */
class TextAttributeSourcePartParser(
    rules: List<TextRule>
) : AttributeSourcePartParser {

    override val type: String = TextAttributeSourcePart.TYPE

    private val trie = Trie.build(rules)

    override fun parse(part: AttributeSourcePart): AttributeParsedSource {
        require(part is TextAttributeSourcePart) {
            "非文本类型无法由 ${this::class.java.simpleName} 解析"
        }

        val matched = part.lines.map { parseLine(it) }

        return toSource(matched)
    }

    fun toSource(results: List<List<TextRuleMatched>>): AttributeParsedSource {
        val parseds = arrayListOf<AttributeParsedSource.Parsed>()

        for ((index, matches) in results.withIndex()) {
            val attributes = arrayListOf<AttributeValue<*>>()
            val conditions = arrayListOf<ParsedCondition>()

            for (matched in matches) {
                when (val target = matched.rule.target) {
                    is TextRule.Target.Attribute -> attributes += AttributeValue(
                        attributeId = target.id,
                        status = Status.of(matched.captures),
                        capture = target.capture,
                    )

                    is TextRule.Target.Condition -> conditions += ParsedCondition(
                        conditionId = target.id,
                        context = ConditionContext(matched.captures)
                    )
                }
            }

            if (conditions.isNotEmpty()) {
                parseds += if (attributes.isEmpty()) {
                    AttributeParsedSource.Parsed.SourceCondition(conditions)
                } else {
                    // 单行条件影响本行产出的所有属性
                    AttributeParsedSource.Parsed.SkipCondition(conditions, attributes.size)
                }
            }

            for (attribute in attributes) {
                parseds += AttributeParsedSource.Parsed.Attribute(attribute)
            }

        }

        return AttributeParsedSource(parseds)
    }

    fun parseLine(line: String): List<TextRuleMatched> {
        // 按起点位置排序后就是从左到右遍历候选结果
        val candidates = trie.find(line).sortedBy { it.startIndex }
        val matches = arrayListOf<TextRuleMatched>()

        var consumed = -1
        var index = 0

        while (index < candidates.size) {
            val start = candidates[index].startIndex
            // 若 start 被其他 rule 匹配通过则退出
            // 比如 "伤害类型 火焰" 和 "火焰 100"
            if (start <= consumed) {
                index++
                continue
            }

            // 如果 start 存在多个可以命中的候选
            // 就取最长的那一个
            // "攻击力 100" 和 "攻击力加成 100"
            var best: TextRuleMatched? = null
            while (index < candidates.size && candidates[index].startIndex == start) {
                val rule = candidates[index].rule
                index++
                // 看看正则能不能通过
                val match = match(line, start, rule) ?: continue
                // 如果 match 的 last 和 best 相等就只能先来后到了
                if (best == null || match.range.last > best.range.last) {
                    best = match
                }
            }

            // 所有候选的正则都不通过才会是 null
            if (best != null) {
                matches += best
                consumed = best.range.last
            }
        }

        return matches
    }

    private fun match(line: String, start: Int, rule: TextRule): TextRuleMatched? {
        for (pattern in rule.compiled) {
            // 基于限定范围重新匹配
            val result = pattern.regex.matchAt(line, start) ?: continue

            val parseValues = parseValues(pattern, result) ?: continue
            return TextRuleMatched(
                rule = rule,
                range = result.range,
                captures = parseValues
            )
        }

        return null
    }

    private fun parseValues(
        pattern: TextPatternTemplate.Compiled,
        matcher: MatchResult
    ): Map<String, Any>? {
        val values = hashMapOf<String, Any>()

        for ((id, value) in pattern.captures) {
            val raw = matcher.groups[id]?.value ?: return null
            val parsed = value.value.parse(raw) ?: return null
            values[id] = parsed
        }

        return values
    }

}