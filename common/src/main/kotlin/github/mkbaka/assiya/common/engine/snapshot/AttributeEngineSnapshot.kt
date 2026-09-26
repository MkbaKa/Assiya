package github.mkbaka.assiya.common.engine.snapshot

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.source.AttributeSourceInfo

data class AttributeEngineSnapshot(
    // 属性 -> 值
    val attributes: Map<String, Status<*>>,
    // 属性源
    val sources: Map<String, AttributeSourceInfo>,
) {

    fun isEmpty() = sources.isEmpty() && attributes.isEmpty()

    fun read(attributeId: String, captureId: String) = attributes[attributeId]?.get(captureId)

    operator fun get(attributeId: String) = attributes[attributeId]

    companion object {

        @JvmStatic
        val EMPTY = AttributeEngineSnapshot(
            attributes = emptyMap(),
            sources = emptyMap()
        )

    }

}