package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.HealComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.HealMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import org.bukkit.entity.LivingEntity
import java.util.function.Consumer

class HealProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
) : BukkitProcessor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(ProcessorComponentBuilder().apply(pcb::accept).build())

    override fun process() {
        HandlerRegistry.publish("heal", this)
        this[CallbackComponent]?.invoke(this)
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(
            entity: LivingEntity,
            value: Double,
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): HealProcessor {
            return HealProcessor { pcb ->
                pcb += BukkitProcessor.defaults()
                pcb += TriggerComponent(entity, AssiyaAPI.getSnapshot(entity))
                pcb += HealMultipleComponent(
                    hashMapOf(
                        "default" to HealComponent("default", value)
                    )
                )

                builder?.accept(pcb)
            }
        }

    }

}
