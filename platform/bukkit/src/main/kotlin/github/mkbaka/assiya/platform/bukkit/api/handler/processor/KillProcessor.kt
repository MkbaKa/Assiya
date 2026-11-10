package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetDefaultComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByKillComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDeathEvent
import java.util.function.Consumer

class KillProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
) : BukkitProcessor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(ProcessorComponentBuilder().apply(pcb::accept).build())

    override fun process() {
        HandlerRegistry.publish("kill", this)
        this[CallbackComponent]?.invoke(this)
    }

    companion object {

        @JvmStatic
        fun of(
            killer: LivingEntity,
            victim: LivingEntity,
            event: EntityDeathEvent,
        ): KillProcessor {
            return KillProcessor { pcb ->
                pcb += BukkitProcessor.defaults()
                pcb += TriggerComponent(killer, AssiyaAPI.getSnapshot(killer))
                pcb += TargetDefaultComponent(victim, AssiyaAPI.getSnapshot(victim))
                pcb += TriggerByKillComponent(event)
            }
        }

    }

}
