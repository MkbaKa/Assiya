package github.mkbaka.assiya.common.engine

import github.mkbaka.assiya.common.engine.state.AttributeEngineState
import github.mkbaka.assiya.common.source.AttributeSourceInfo
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue

/**
 * update 事件触发中对属性的操作
 * 如果单独做命令提交会反复触发 update  应该攒攒在当前提交中一块执行
 */
class AttributeUpdateContext(
    val workingState: AttributeEngineState
) {

    private val commands = arrayListOf<AttributeEngineAction>()

    operator fun get(sourceId: String): AttributeSourceInfo? {
        return workingState.sourceIndex[sourceId]
    }

    fun submit(patch: AttributeSourcePatch) {
        val accepted = patch.copy(
            attributes = patch.attributes.toList()
        )

        workingState.update(accepted)

        commands += AttributeEngineAction.UpdateSource(
            patch = accepted
        )
    }

    fun modify(
        sourceId: String,
        attributes: List<AttributeValue<*>>,
        priority: Int? = null,
        create: Boolean = false,
        lifeCycle: AttributeSourceLifecycle? = null
    ) {
        val previous = workingState.sourceIndex[sourceId]
        if (previous == null && !create) return

        if (previous == null || lifeCycle != null) {
            submit(
                AttributeSourcePatch(
                    sourceId = sourceId,
                    priority = requireNotNull(priority ?: previous?.priority) {
                        "Priority is required to create attribute source: $sourceId"
                    },
                    lifeCycle = lifeCycle ?: AttributeSourceLifecycle.Stable,
                    attributes = attributes
                )
            )
            return
        }

        val accepted = attributes.toList()
        workingState.modify(sourceId, accepted, priority)

        commands += AttributeEngineAction.ModifySource(
            sourceId = sourceId,
            attributes = accepted,
            priority = priority
        )
    }

    fun remove(sourceId: String) {
        if (!workingState.remove(sourceId).success) {
            return
        }

        commands += AttributeEngineAction.RemoveSource(sourceId)
    }

    fun commands(): List<AttributeEngineAction> {
        return commands.toList()
    }

}
