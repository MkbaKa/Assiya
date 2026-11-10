package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent
import org.bukkit.event.entity.EntityDeathEvent

class TriggerByKillComponent(
    val event: EntityDeathEvent,
    var reviveHealth: Double? = null,
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<TriggerByKillComponent>

}
