package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import org.bukkit.event.entity.EntityDamageByEntityEvent

class TriggerByEventComponent(
    val event: EntityDamageByEntityEvent
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<TriggerByEventComponent>

}