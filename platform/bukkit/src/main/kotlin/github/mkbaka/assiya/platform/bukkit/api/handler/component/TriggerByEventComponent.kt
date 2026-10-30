package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class TriggerByEventComponent(
    val event: EntityDamageByEntityEvent
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    val isBlocking: Boolean
        get() = event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) > 0

    companion object Key : ProcessorComponent.Key<TriggerByEventComponent>

}