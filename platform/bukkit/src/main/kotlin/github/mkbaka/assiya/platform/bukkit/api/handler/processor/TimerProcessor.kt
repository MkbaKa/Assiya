package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry.Subscriber
import org.bukkit.entity.Entity
import java.util.function.Consumer

class TimerProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
    val subscribers: List<Subscriber>
) : BukkitProcessor {

    constructor(subscribers: List<Subscriber>, pcb: Consumer<ProcessorComponentBuilder>) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build(),
        subscribers
    )

    override fun process() {
        HandlerRegistry.publish(subscribers, this)
        this[CallbackComponent]?.invoke(this)
    }

    companion object {

        @JvmStatic
        fun of(entity: Entity, snapshot: AttributeEngineSnapshot, subscribers: List<Subscriber>): TimerProcessor {
            return TimerProcessor(subscribers) { pcb ->
                pcb += BukkitProcessor.defaults()
                pcb += TriggerComponent(entity, snapshot)
            }
        }

    }

}
