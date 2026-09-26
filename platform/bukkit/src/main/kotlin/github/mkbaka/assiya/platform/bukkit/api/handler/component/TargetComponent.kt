package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.EntityProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import org.bukkit.entity.LivingEntity

/**
 * 若该组件存在 说明本次 processor 是由 trigger & entity 互动触发的
 * 比如 EntityDamageByEntityEvent
 *
 * @param [entity]
 * @param [snapshot]
 */
class TargetComponent(
    override val entity: LivingEntity,
    override val snapshot: AttributeEngineSnapshot,
    attribute: AttributeComponent = DefaultAttributeComponent(snapshot)
) : ProcessorComponent, EntityProcessorComponent, AttributeComponent by attribute {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<TargetComponent>

}