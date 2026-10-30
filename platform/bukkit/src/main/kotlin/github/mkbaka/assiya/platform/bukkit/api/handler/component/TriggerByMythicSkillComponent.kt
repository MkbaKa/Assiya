package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent

interface TriggerByMythicSkillComponent : ProcessorComponent {

    override val key: ProcessorComponent.Key<*>
        get() = Key

    /**
     * 用于跨版本标识
     * 具体字段得强转一下
     *
     * 参考 [TargetComponent.Key]
     */
    companion object Key : ProcessorComponent.Key<TriggerByMythicSkillComponent>

}