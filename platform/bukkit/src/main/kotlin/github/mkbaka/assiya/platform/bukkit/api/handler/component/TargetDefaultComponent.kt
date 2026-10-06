package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import org.bukkit.entity.LivingEntity

/**
 * 若该组件存在 说明本次 processor 是由 trigger & entity 互动触发的
 * 比如 EntityDamageByEntityEvent
 *
 * @param [entity]
 * @param [snapshot]
 */
class TargetDefaultComponent(
    override val entity: LivingEntity,
    override val snapshot: AttributeEngineSnapshot,
    attribute: AttributeContainer = DefaultAttributeContainer(snapshot)
) : TargetComponent, AttributeContainer by attribute