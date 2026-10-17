package github.mkbaka.assiya.common.engine

import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue

/**
 * 消息类型
 */
interface AttributeEngineAction {

    data class UpdateSource(
        val patch: AttributeSourcePatch
    ) : AttributeEngineAction

    /**
     * 当 sourceId 已存在时
     *      若 priority 不为 null 则覆盖旧值
     *          若为 null 则使用旧值
     *      lifeCycle 不为 null 同理
     *
     * 当 sourceId 不存在时 若 create = false 则不做任何操作
     *      反之等价于 UpdateSource 创建新来源
     */
    data class ModifySource(
        val sourceId: String,
        val attributes: Map<String, AttributeValue<*>>,
        val priority: Int? = null,
        val create: Boolean = false,
        val lifeCycle: AttributeSourceLifecycle? = null
    ) : AttributeEngineAction

    data class RemoveSource(val sourceId: String) : AttributeEngineAction

    data class Refresh(val triggerEvent: Boolean = true) : AttributeEngineAction

    data object CheckExpiration : AttributeEngineAction

    data class Batch(
        val actions: List<AttributeEngineAction>
    ) : AttributeEngineAction

}
