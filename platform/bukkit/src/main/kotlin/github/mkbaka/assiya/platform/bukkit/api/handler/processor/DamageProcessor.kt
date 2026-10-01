package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.function.Consumer

/**
 * 一次伤害处理的过程
 */
class DamageProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>
) : Processor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(ProcessorComponentBuilder().apply(pcb::accept).build())

    override fun process() {
        HandlerRegistry.publish("damage", this)
        this[CallbackComponent.Key]?.invoke(this)
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(
            attacker: Entity,
            defender: LivingEntity,
            attackerSnapshot: AttributeEngineSnapshot,
            defenderSnapshot: AttributeEngineSnapshot,
            attributes: Map<String, Status<Any>> = emptyMap(),
            clear: Boolean = false,
            overwrite: Boolean = false,
            disabledAttributes: Collection<String> = emptyList(),
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageProcessor {
            val container = DefaultAttributeContainer.of(attackerSnapshot, attributes, clear, overwrite)
            return DamageProcessor { pcb ->
                pcb += Processor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot, container)
                pcb += TargetComponent(defender, defenderSnapshot)
                pcb += DamageMultipleComponent()
                for (id in disabledAttributes) {
                    pcb[CancellableAttributeComponent]!!.setCancelled(id)
                }
                builder?.accept(pcb)
            }
        }

        @JvmStatic
        fun ofAttack(
            attacker: Entity,
            defender: LivingEntity,
            attackerSnapshot: AttributeEngineSnapshot,
            defenderSnapshot: AttributeEngineSnapshot,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return of(attacker, defender, attackerSnapshot, defenderSnapshot, builder = builder)
        }

        @JvmStatic
        fun ofProjectile(
            projectile: Entity,
            defender: LivingEntity,
            projectileSnapshot: AttributeEngineSnapshot,
            defenderSnapshot: AttributeEngineSnapshot,
            shooter: TriggerComponent,
            force: Float,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return DamageProcessor { pcb ->
                pcb += Processor.defaults()
                val trigger = TriggerComponent(projectile, projectileSnapshot)
                pcb += trigger
                pcb += TargetComponent(defender, defenderSnapshot)
                pcb += ProjectileComponent(shooter, trigger, force)
                pcb += DamageMultipleComponent()
                builder?.accept(pcb)
            }
        }

    }

}
