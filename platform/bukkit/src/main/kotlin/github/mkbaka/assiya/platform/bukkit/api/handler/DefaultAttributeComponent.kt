package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.state.AttributeEngineState
import github.mkbaka.assiya.common.engine.state.AttributeState
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry

/**
 * 供 processor 过程中临时操作属性
 */
open class DefaultAttributeComponent(
    private val base: AttributeEngineState
) : AttributeComponent {

    constructor(snapshot: AttributeEngineSnapshot) : this(
        AttributeEngineState.from(snapshot)
    )

    private val temporary = AttributeEngineState()

    override fun addAttribute(
        attributeId: String,
        captureId: String,
        value: Any,
        sourceId: String,
        priority: Int?
    ) {
        addAttribute(
            attributeId,
            Status(captureId to value),
            sourceId,
            priority
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun addAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String,
        priority: Int?
    ) {
        val reader = findReader(attributeId) ?: return
        val source = temporary.sourceIndex[sourceId]

        val attributes = linkedMapOf<String, AttributeValue<*>>()

        for (attribute in source?.attributes.orEmpty()) {
            attributes[attribute.attributeId] = attribute
        }

        val previous =
            attributes[attributeId]?.status as? Status<Any>

        attributes[attributeId] = AttributeValue(
            attributeId = attributeId,
            status = if (previous == null) {
                status
            } else {
                reader.capture.merge(previous, status)
            },
            capture = reader.capture
        )

        temporary.advanceSequence(base.currentSequence())

        temporary.update(
            AttributeSourcePatch(
                sourceId = sourceId,
                priority = priority ?: source?.priority ?: 0,
                lifeCycle = AttributeSourceLifecycle.Stable,
                attributes = attributes.values.toList()
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun status(attributeId: String): Status<Any>? {
        val original =
            base.attributeIndex[attributeId] as? AttributeState<Any>

        val extra =
            temporary.attributeIndex[attributeId] as? AttributeState<Any>

        return when {
            original != null -> original.snapshot(extra)
            extra != null -> extra.snapshot()
            else -> null
        }?.takeUnless { it.isEmpty }
    }

    override fun hasAttribute(attributeId: String): Boolean {
        return attributeId in base.attributeIndex || attributeId in temporary.attributeIndex
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getValue(
        attributeId: String,
        captureId: String
    ): T? {
        val reader = findReader(attributeId) ?: return null
        val status = status(attributeId) ?: return null

        return reader.read(status, captureId) as? T
    }

    private fun findReader(attributeId: String) =
        AttributeRegistry[attributeId]
            ?.get(ReaderComponent.Key)
            ?.reader
}