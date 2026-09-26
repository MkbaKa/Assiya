package github.mkbaka.assiya.common.condition

/**
 * 解析完成 待判断的条件
 */
data class ParsedCondition(
    val conditionId: String,
    val context: ConditionContext
)