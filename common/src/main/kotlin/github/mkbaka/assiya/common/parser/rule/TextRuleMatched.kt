package github.mkbaka.assiya.common.parser.rule

/**
 * 单行文本里的 rule 命中
 */
data class TextRuleMatched(
    val rule: TextRule,
    val range: IntRange,
    val captures: Map<String, Any>
)