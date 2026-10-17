package github.mkbaka.assiya.common.source

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status

@Suppress("UNCHECKED_CAST")
fun mergeAttributeValues(
    values: Iterable<AttributeValue<*>>
): Map<String, AttributeValue<*>> {
    val merged = linkedMapOf<String, AttributeValue<*>>()
    // 先按 attribute id 分类
    for ((attributeId, group) in values.groupBy { it.attributeId }) {
        // 为了拿 capture
        val first = group.first() as AttributeValue<Any>
        // 这一属性所有的 status
        val statuses = group.map { (it as AttributeValue<Any>).status }
        // 合并结果
        merged[attributeId] = first.copy(status = first.capture.merge(statuses))
    }
    return merged
}

fun mergeAttribute(
    attributes: Map<String, AttributeValue<*>>,
    attributeId: String,
    status: Status<Any>,
    capture: Capture<Any>
): Map<String, AttributeValue<*>> {
    val merged = attributes.toMutableMap()
    val previous = merged[attributeId]?.status as? Status<Any>
    // 如果没属性就直接用 status 作为结果  反之需要合并两个结果
    merged[attributeId] = AttributeValue(
        attributeId = attributeId,
        status = if (previous == null) status else capture.merge(previous, status),
        capture = capture
    )
    return merged
}
