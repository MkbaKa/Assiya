package github.mkbaka.assiya.common.engine

import github.mkbaka.assiya.common.engine.state.AttributeEngineState
import github.mkbaka.assiya.common.source.AttributeSourceInfo
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue

/**
 * 本轮刷新的属性草稿与来源操作记录
 * submit, modify 和 remove 立即修改 workingState, 让同一轮后续 handler 读取到修改结果.
 * 来源操作在更新事件未取消时统一回放到引擎, 再发布快照; 取消时整轮草稿丢弃.
 * 单独提交 Refresh(false) 可以避免再次触发 update, 但会排队到后续刷新,
 * 不会修改当前草稿, 也不受本轮更新事件的取消结果约束.
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
            attributes = patch.attributes.toMap()
        )

        workingState.update(accepted)

        commands += AttributeEngineAction.UpdateSource(
            patch = accepted
        )
    }

    fun modify(
        sourceId: String,
        attributes: Map<String, AttributeValue<*>>,
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

        val accepted = attributes.toMap()
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
