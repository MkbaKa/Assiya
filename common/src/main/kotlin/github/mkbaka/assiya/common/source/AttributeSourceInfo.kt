package github.mkbaka.assiya.common.source

/**
 * 一条记录在引擎里的属性来源
 *
 * @param [sourceId] 属性源
 * @param [priority] 优先级
 * @param [sequence] 当前引擎内的变动次数
 * @param [attributes] 影响的属性
 */
data class AttributeSourceInfo(
    val sourceId: String,
    val priority: Int,
    val sequence: Long,
    val attributes: Map<String, AttributeValue<*>>
)
