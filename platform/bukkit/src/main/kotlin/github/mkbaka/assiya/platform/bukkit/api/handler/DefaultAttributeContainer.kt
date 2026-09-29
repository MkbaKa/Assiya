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
class DefaultAttributeContainer(
    private val base: AttributeEngineState
) : AttributeContainer {

    constructor(snapshot: AttributeEngineSnapshot) : this(
        AttributeEngineState.from(snapshot)
    )

    private val temporary = AttributeEngineState()
    private val overwritten = hashSetOf<String>()

    @JvmOverloads
    fun setAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String = "processor:temporary",
        priority: Int? = null
    ) {
        if (findReader(attributeId) == null) return

        /**
         * 删掉临时来源中对于该属性的操作
         * 然后使读取时忽略 base 中该属性的值
         */
        val sourceIds = temporary.attributeIndex[attributeId]?.sourceIds().orEmpty()
        for (id in sourceIds) {
            val source = temporary.sourceIndex[id]!!
            temporary.modify(
                id,
                source.attributes.filterNot { it.attributeId == attributeId }
            )
        }
        overwritten.add(attributeId)

        addAttribute(attributeId, status, sourceId, priority)
    }

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

        // 让临时来源的 sequence 与 base 同步
        // 这样 update 时 patch 的 sequence 就不会跑到 base 前面 导致新值比旧值先计算
        temporary.sequence = maxOf(temporary.sequence, base.sequence)

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
            if (attributeId in overwritten) null else base.attributeIndex[attributeId] as? AttributeState<Any>

        val extra =
            temporary.attributeIndex[attributeId] as? AttributeState<Any>

        return when {
            original != null -> original.snapshot(extra)
            extra != null -> extra.snapshot()
            else -> null
        }?.takeUnless { it.isEmpty }
    }

    override fun hasAttribute(attributeId: String): Boolean {
        return (attributeId !in overwritten && attributeId in base.attributeIndex)
            || attributeId in temporary.attributeIndex
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

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(
            snapshot: AttributeEngineSnapshot,
            attributes: Map<String, Status<Any>>,
            clear: Boolean = false,
            overwrite: Boolean = false
        ): DefaultAttributeContainer {
            val container = DefaultAttributeContainer(if (clear) AttributeEngineSnapshot.EMPTY else snapshot)
            for ((id, status) in attributes) {
                if (overwrite) {
                    container.setAttribute(id, status)
                } else {
                    container.addAttribute(id, status)
                }
            }
            return container
        }

    }

}
