package github.mkbaka.assiya.common.engine.state

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.source.AttributeSourceInfo
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue

/**
 * 引擎中的所有属性值管理
 *      非线程安全
 */
class AttributeEngineState {

    var sequence = 0L

    val sourceIndex: Map<String, AttributeSourceInfo>
        field = linkedMapOf()

    val attributeIndex: Map<String, AttributeState<*>>
        field = linkedMapOf<String, AttributeState<*>>()

    fun copy(): AttributeEngineState {
        return AttributeEngineState().also { result ->
            result.sequence = sequence

            for ((sourceId, source) in sourceIndex) {
                result.sourceIndex[sourceId] = source.copy(
                    attributes = source.attributes.toMap()
                )
            }

            for ((attributeId, attribute) in attributeIndex) {
                result.attributeIndex[attributeId] = attribute.copy()
            }
        }
    }

    fun update(patch: AttributeSourcePatch): AttributeSourceDiff {
        return replace(
            sourceId = patch.sourceId,
            priority = patch.priority,
            attributes = patch.attributes
        )
    }

    fun modify(
        sourceId: String,
        attributes: Map<String, AttributeValue<*>>,
        priority: Int? = null
    ): AttributeSourceDiff {
        val previous = requireNotNull(sourceIndex[sourceId]) {
            "Unknown attribute source: $sourceId"
        }

        return replace(
            sourceId = sourceId,
            priority = priority ?: previous.priority,
            attributes = attributes
        )
    }

    fun remove(sourceId: String): AttributeSourceRemoveResult {
        val maybe = sourceIndex.remove(sourceId)
            ?: return AttributeSourceRemoveResult(false, emptySet())

        val affected = maybe.attributes.keys.toSet()
        for (attributeId in maybe.attributes.keys) {
            removeAttr(attributeId, sourceId)
        }

        return AttributeSourceRemoveResult(true, affected)
    }

    fun removeAttributeContribution(sourceId: String, attributeId: String) {
        val source = sourceIndex[sourceId] ?: return
        sourceIndex[sourceId] = source.copy(
            attributes = source.attributes - attributeId
        )
        removeAttr(attributeId, sourceId)
    }

    fun replace(
        sourceId: String,
        priority: Int,
        attributes: Map<String, AttributeValue<*>>
    ): AttributeSourceDiff {
        val previous = sourceIndex[sourceId]
        val affected = linkedSetOf<String>()

        if (previous != null) {
            for (attributeId in previous.attributes.keys) {
                affected += attributeId
                removeAttr(attributeId, sourceId)
            }
        }

        val source = AttributeSourceInfo(
            sourceId = sourceId,
            priority = priority,
            sequence = ++sequence,
            attributes = attributes
        )

        sourceIndex[sourceId] = source

        for ((attributeId, attribute) in source.attributes) {
            affected += attributeId
            setAttr(source, attribute)
        }

        return AttributeSourceDiff(
            affectedAttributes = affected,
            changedSources = setOf(sourceId)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> setAttr(source: AttributeSourceInfo, attribute: AttributeValue<T>) {
        val state = attributeIndex.computeIfAbsent(attribute.attributeId) {
            AttributeState(attribute.capture)
        } as AttributeState<T>
        state.put(source, attribute.status)
    }

    private fun removeAttr(attributeId: String, sourceId: String) {
        val state = attributeIndex[attributeId] ?: return
        state.remove(sourceId)
        if (state.isEmpty()) {
            attributeIndex.remove(attributeId)
        }
    }

    data class AttributeSourceDiff(
        val affectedAttributes: Set<String>,
        val changedSources: Set<String>
    )

    data class AttributeSourceRemoveResult(
        val success: Boolean,
        val affectedAttributes: Set<String>
    )

    companion object {

        fun from(snapshot: AttributeEngineSnapshot): AttributeEngineState {
            val state = AttributeEngineState()

            for ((sourceId, source) in snapshot.sources) {
                state.sourceIndex[sourceId] = source
                state.sequence = maxOf(state.sequence, source.sequence)

                for (attribute in source.attributes.values) {
                    state.setAttr(source, attribute)
                }
            }

            return state
        }
    }

}
