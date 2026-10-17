package github.mkbaka.assiya.common.source

/**
 * 属性来源
 *
 * @param [sourceId] 源名称
 * @param [priority] 优先级
 * @param [lifeCycle] 生命周期
 * @param [attributes] attribute id -> attribute value
 */
data class AttributeSourcePatch(
    val sourceId: String,
    val priority: Int,
    val lifeCycle: AttributeSourceLifecycle,
    val attributes: Map<String, AttributeValue<*>>
)
