package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.source.AttributeValue

@Suppress("UNCHECKED_CAST")
internal fun mergeAttribute(
    attributes: List<AttributeValue<*>>,
    attributeId: String,
    status: Status<Any>,
    capture: Capture<Any>
): List<AttributeValue<*>> {
    val merged = linkedMapOf<String, AttributeValue<*>>()
    for (attribute in attributes) {
        merged[attribute.attributeId] = attribute
    }
    val previous = merged[attributeId]?.status as? Status<Any>
    merged[attributeId] = AttributeValue(
        attributeId = attributeId,
        status = if (previous == null) status else capture.merge(previous, status),
        capture = capture
    )
    return merged.values.toList()
}
