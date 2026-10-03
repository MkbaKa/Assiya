package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.damage.DamageProcessorStack
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
        val uuid = getComponent(TriggerComponent).entity.uniqueId
        DamageProcessorStack.push(uuid, this)
        try {
            HandlerRegistry.publish("damage", this)
            this[CallbackComponent.Key]?.invoke(this)
        } finally {
            DamageProcessorStack.pop(uuid)
        }
    }

    companion object {

        @JvmStatic
        fun of(
            attacker: Entity,
            defender: LivingEntity,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return ofSnapshot(attacker, defender, builder = builder)
        }

        @JvmStatic
        @JvmOverloads
        fun ofSnapshot(
            attacker: Entity,
            defender: LivingEntity,
            attackerSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(attacker),
            defenderSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(defender),
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageProcessor {
            return DamageProcessor { pcb ->
                pcb += Processor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot)
                pcb += TargetComponent(defender, defenderSnapshot)
                pcb += DamageMultipleComponent()
                builder?.accept(pcb)
            }
        }

        @JvmStatic
        fun ofProjectile(
            projectile: Entity,
            defender: LivingEntity,
            shooter: Entity,
            force: Float,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return of(projectile, defender) { pcb ->
                pcb += ProjectileComponent(
                    TriggerComponent(shooter, AssiyaAPI.getSnapshot(shooter)),
                    pcb[TriggerComponent.Key]!!,
                    force
                )
                builder?.accept(pcb)
            }
        }

    }

}
