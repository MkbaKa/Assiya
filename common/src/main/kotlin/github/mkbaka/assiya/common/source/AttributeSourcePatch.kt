package github.mkbaka.assiya.common.source

/**
 * 属性来源
 *
 * @param [sourceId] 源名称
 * @param [priority] 优先级
 * @param [lifeCycle] 生命周期
 * @param [attributes] 影响的属性
 */
data class AttributeSourcePatch(
    val sourceId: String,
    val priority: Int,
    val lifeCycle: AttributeSourceLifecycle,
    val attributes: List<AttributeValue<*>>
)