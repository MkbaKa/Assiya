package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetDefaultComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByEventComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.function.Consumer

class KillProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
) : Processor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(ProcessorComponentBuilder().apply(pcb::accept).build())

    override fun process() {
        HandlerRegistry.publish("kill", this)
        this[CallbackComponent]?.invoke(this)
    }

    companion object {

        @JvmStatic
        fun of(
            killer: Entity,
            victim: LivingEntity,
            event: EntityDamageByEntityEvent,
        ): KillProcessor {
            return KillProcessor { pcb ->
                pcb += Processor.defaults()
                pcb += TriggerComponent(killer, AssiyaAPI.getSnapshot(killer))
                pcb += TargetDefaultComponent(victim, AssiyaAPI.getSnapshot(victim))
                pcb += TriggerByEventComponent(event)
            }
        }

    }

}
