package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.state.AttributeEngineState
import github.mkbaka.assiya.common.engine.state.AttributeState
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.mergeAttribute

/**
 * 供 processor 过程中临时操作属性
 */
class DefaultAttributeContainer(
    private val base: AttributeEngineState
) : AttributeContainer {

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

    override fun addAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String,
        priority: Int?
    ) {
        val reader = findReader(attributeId) ?: return
        val source = temporary.sourceIndex[sourceId]

        val attributes = mergeAttribute(source?.attributes.orEmpty(), attributeId, status, reader.capture)

        // 让临时来源的 sequence 与 base 同步
        // 这样 update 时 patch 的 sequence 就不会跑到 base 前面 导致新值比旧值先计算
        temporary.sequence = maxOf(temporary.sequence, base.sequence)

        temporary.update(
            AttributeSourcePatch(
                sourceId = sourceId,
                priority = priority ?: source?.priority ?: 0,
                lifeCycle = AttributeSourceLifecycle.Stable,
                attributes = attributes
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun status(attributeId: String): Status<Any>? {
        val original = base.attributeIndex[attributeId] as? AttributeState<Any>

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
