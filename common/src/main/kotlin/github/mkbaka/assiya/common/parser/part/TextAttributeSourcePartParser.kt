package github.mkbaka.assiya.common.parser.part

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.condition.ConditionContext
import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.RuleSet
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate
import github.mkbaka.assiya.common.parser.text.TextRule
import github.mkbaka.assiya.common.parser.text.TextRuleMatched
import github.mkbaka.assiya.common.parser.text.Trie
import github.mkbaka.assiya.common.source.AttributeValue
import java.util.regex.Matcher

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
        val attributesByPath = linkedMapOf<String, AttributeValue<*>>()
        val parsedConditions = arrayListOf<AttributeParsedSource.Condition>()

        for ((lineIndex, matches) in results.withIndex()) {
            // 用 "行号.本行序号" 保存每一条贡献 避免同一条属性多次出现时互相覆盖.
            var attributeIndex = 0
            val conditions = arrayListOf<ParsedCondition>()

            for (matched in matches) {
                when (val target = matched.rule.target) {
                    is TextRule.Target.Attribute -> {
                        attributesByPath["$lineIndex.$attributeIndex"] = AttributeValue(
                            attributeId = target.id,
                            status = Status.of(matched.captures),
                            capture = target.capture,
                        )
                        attributeIndex++
                    }

                    is TextRule.Target.Condition -> conditions += ParsedCondition(
                        conditionId = target.id,
                        context = ConditionContext(matched.captures)
                    )
                }
            }

            if (conditions.isNotEmpty()) {
                // 如果这一行有属性 说明它是一个单行条件
                // 反之为整个来源级的条件
                parsedConditions += AttributeParsedSource.Condition(
                    conditions,
                    if (attributeIndex == 0) emptyList() else listOf(lineIndex.toString())
                )
            }

        }

        return AttributeParsedSource(listOf(AttributeParsedSource.Part(attributesByPath, parsedConditions)))
    }

    fun parseLine(line: String): List<TextRuleMatched> {
        // 命中属性名的候选
        val candidates = trie.find(line)
            .asSequence()
            .sortedBy { it.startIndex }
            .map { it.rule }
            .distinct()

        val matches = arrayListOf<TextRuleMatched>()
        var cursor = 0

        while (cursor < line.length) {
            var best: TextRuleMatched? = null
            for (rule in candidates) {
                val match = match(line, cursor, rule) ?: continue
                // 优先使用更靠左的规则
                // 用来解决 "物理伤害 100 护甲" 这种情况
                if (best == null || match.range.first < best.range.first ||
                    (match.range.first == best.range.first && match.range.last > best.range.last)
                ) {
                    best = match
                }
            }

            val accepted = best ?: break
            matches += accepted

            cursor = accepted.range.last + 1
        }

        return matches
    }

    private fun match(line: String, start: Int, rule: TextRule): TextRuleMatched? {
        var best: TextRuleMatched? = null
        for (pattern in rule.compiled) {
            val matcher = pattern.regex.toPattern().matcher(line)
                // 限制匹配区域
                .region(start, line.length)
                .useTransparentBounds(false)
                .useAnchoringBounds(false)

            while (matcher.find()) {
                if (matcher.start() == matcher.end()) continue
                if (best != null && matcher.start() >= best.range.first) break
                val values = parseValues(pattern, matcher) ?: continue
                best = TextRuleMatched(
                    rule = rule,
                    range = matcher.start() until matcher.end(),
                    captures = values
                )
                break
            }
        }

        return best
    }

    companion object {

        fun compile(rules: RuleSet): TextAttributeSourcePartParser {
            val textRules = arrayListOf<TextRule>()
            for (rule in rules.attributes) {
                textRules += TextRule(
                    names = rule.names,
                    capture = rule.capture,
                    patterns = rule.patterns,
                    target = TextRule.Target.Attribute(rule.id, rule.capture)
                )
            }
            for (rule in rules.conditions) {
                if (rule.names.isEmpty()) continue
                textRules += TextRule(
                    names = rule.names,
                    capture = rule.capture,
                    patterns = rule.patterns,
                    target = TextRule.Target.Condition(rule.id)
                )
            }
            return TextAttributeSourcePartParser(textRules)
        }

    }

    private fun parseValues(
        pattern: TextPatternTemplate.Compiled,
        matcher: Matcher
    ): Map<String, Any>? {
        val values = hashMapOf<String, Any>()

        for ((id, value) in pattern.captures) {
            val raw = matcher.group(id) ?: return null
            val parsed = value.value.parse(raw) ?: return null
            values[id] = parsed
        }

        return values
    }

}
