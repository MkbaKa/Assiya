package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import org.bukkit.entity.Entity
import java.util.function.Consumer

class UpdateProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
    val context: AttributeUpdateContext
) : BukkitProcessor {

    constructor(context: AttributeUpdateContext, pcb: Consumer<ProcessorComponentBuilder>) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build(),
        context
    )

    override fun process() {
        HandlerRegistry.publish("update", this)
        this[CallbackComponent]?.invoke(this)
    }

    companion object {

        @JvmStatic
        fun of(
            entity: Entity,
            snapshot: AttributeEngineSnapshot,
            context: AttributeUpdateContext
        ): UpdateProcessor {
            return UpdateProcessor(context) { pcb ->
                pcb += BukkitProcessor.defaults()
                pcb += TriggerComponent(entity, snapshot, DefaultAttributeContainer(context.workingState))
            }
        }

    }

}