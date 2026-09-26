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

    private var sequence = 0L

    val sourceIndex: Map<String, AttributeSourceInfo>
        field = linkedMapOf()

    val attributeIndex: Map<String, AttributeState<*>>
        field = linkedMapOf<String, AttributeState<*>>()

    fun copy(): AttributeEngineState {
        return AttributeEngineState().also { result ->
            result.sequence = sequence

            for ((sourceId, source) in sourceIndex) {
                result.sourceIndex[sourceId] = source.copy(
                    attributes = source.attributes.toList()
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
        attributes: List<AttributeValue<*>>,
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

        val affected = maybe.attributes.mapTo(linkedSetOf()) { it.attributeId }
        for (contribution in maybe.attributes) {
            removeAttr(contribution.attributeId, sourceId)
        }

        return AttributeSourceRemoveResult(true, affected)
    }

    private fun replace(
        sourceId: String,
        priority: Int,
        attributes: List<AttributeValue<*>>
    ): AttributeSourceDiff {
        val previous = sourceIndex[sourceId]
        val affected = linkedSetOf<String>()

        if (previous != null) {
            for (attribute in previous.attributes) {
                affected += attribute.attributeId
                removeAttr(attribute.attributeId, sourceId)
            }
        }

        val source = AttributeSourceInfo(
            sourceId = sourceId,
            priority = priority,
            sequence = ++sequence,
            attributes = attributes.toList()
        )

        sourceIndex[sourceId] = source

        for (attribute in source.attributes) {
            affected += attribute.attributeId
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
            AttributeState<T>(
                attribute.capture
            )
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

    // TODO 什么鬼玩意
    fun advanceSequence(minimum: Long) {
        sequence = maxOf(sequence, minimum)
    }

    fun currentSequence(): Long = sequence

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
                val copied = source.copy(
                    attributes = source.attributes.toList()
                )

                state.sourceIndex[sourceId] = copied
                state.sequence = maxOf(state.sequence, copied.sequence)

                for (attribute in copied.attributes) {
                    state.setAttr(copied, attribute)
                }
            }

            return state
        }
    }

}