package github.mkbaka.assiya.platform.bukkit.api.attribute

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.state.AttributeEngineState
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.common.source.mergeAttribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry

/**
 * 基于现有快照做一层新的快照
 */
class AttributeSnapshotBuilder @JvmOverloads constructor(
    snapshot: AttributeEngineSnapshot = AttributeEngineSnapshot.EMPTY
) {

    private val state = AttributeEngineState.from(snapshot)

    @JvmOverloads
    fun addAttribute(
        attributeId: String,
        captureId: String,
        value: Any,
        sourceId: String = "snapshot:temporary",
        priority: Int? = null
    ) {
        addAttribute(attributeId, Status(captureId to value), sourceId, priority)
    }

    @JvmOverloads
    fun addAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String = "snapshot:temporary",
        priority: Int? = null
    ) {
        val reader = AttributeRegistry[attributeId]?.get(ReaderComponent)?.reader ?: return
        addAttribute(attributeId, status, sourceId, priority, reader.capture)
    }

    @JvmOverloads
    fun setAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String = "snapshot:temporary",
        priority: Int? = null
    ) {
        val reader = AttributeRegistry[attributeId]?.get(ReaderComponent)?.reader ?: return
        val sourceIds = state.attributeIndex[attributeId]?.sourceIds().orEmpty()
        for (id in sourceIds) {
            state.removeAttributeContribution(id, attributeId)
        }
        addAttribute(attributeId, status, sourceId, priority, reader.capture)
    }

    private fun addAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String,
        priority: Int?,
        capture: Capture<Any>
    ) {
        val source = state.sourceIndex[sourceId]
        val attributes = mergeAttribute(source?.attributes.orEmpty(), attributeId, status, capture)
        replaceSource(sourceId, attributes, priority)
    }

    @JvmOverloads
    fun replaceSource(
        sourceId: String,
        attributes: Map<String, AttributeValue<*>>,
        priority: Int? = null
    ) {
        state.replace(sourceId, priority ?: state.sourceIndex[sourceId]?.priority ?: 0, attributes)
    }

    fun removeSource(sourceId: String) {
        state.remove(sourceId)
    }

    fun build(): AttributeEngineSnapshot {
        val attributes = linkedMapOf<String, Status<*>>()
        for ((id, attribute) in state.attributeIndex) {
            val status = attribute.snapshot()
            if (!status.isEmpty) {
                attributes[id] = status
            }
        }
        return AttributeEngineSnapshot(attributes, state.sourceIndex.toMap())
    }

}
