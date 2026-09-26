package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.function.Consumer

class DamageProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>
) : Processor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(ProcessorComponentBuilder().apply(pcb::accept).build())

    override fun process() {
        try {
            this[TriggerComponent.Key]?.let { it.session?.damageProcessorStack?.push(processor = this) }
            HandlerRegistry.publish("damage", this)

            this[CallbackComponent.Key]?.invoke(this)
        } finally {
            this[TriggerComponent.Key]?.let { it.session?.damageProcessorStack?.pop() }
        }
    }

    companion object {

        @JvmStatic
        fun of(
            attacker: Entity,
            defender: LivingEntity,
            attackerSnapshot: AttributeEngineSnapshot,
            defenderSnapshot: AttributeEngineSnapshot,
            builder: Consumer<DamageProcessor>
        ): DamageProcessor {
            return DamageProcessor { pcb ->
                pcb += Processor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot)
                pcb += TargetComponent(defender, defenderSnapshot)
                pcb += DamageMultipleComponent()
            }.apply(builder::accept)
        }

        @JvmStatic
        fun ofProjectile(
            projectile: Entity,
            defender: LivingEntity,
            projectileSnapshot: AttributeEngineSnapshot,
            defenderSnapshot: AttributeEngineSnapshot,
            shooter: TriggerComponent,
            force: Float,
            builder: Consumer<DamageProcessor>
        ): DamageProcessor {
            return DamageProcessor { pcb ->
                pcb += Processor.defaults()
                val trigger = TriggerComponent(projectile, projectileSnapshot)
                pcb += trigger
                pcb += TargetComponent(defender, defenderSnapshot)
                pcb += ProjectileComponent(shooter, trigger, force)
                pcb += DamageMultipleComponent()
            }.apply(builder::accept)
        }

    }

}
