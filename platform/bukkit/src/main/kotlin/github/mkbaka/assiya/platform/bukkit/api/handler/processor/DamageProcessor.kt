package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.ForkableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry.Subscriber
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import java.util.function.Consumer

/**
 * 一次伤害处理的过程
 * 可以通过 [HandlerRegistry.select] 提前筛出允许执行的 subscriber
 */
class DamageProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
    val subscribers: List<Subscriber>? = null
) : Processor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build()
    )

    constructor(
        subscribers: List<Subscriber>?,
        pcb: Consumer<ProcessorComponentBuilder>
    ) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build(),
        subscribers
    )

    override fun process() {
        val selected = subscribers
        if (selected == null) {
            HandlerRegistry.publish("damage", this)
        } else {
            HandlerRegistry.publish(selected, this)
        }
        this[CallbackComponent.Key]?.invoke(this)
    }

    /**
     * 基于当前组件数据创建新的 Processor
     *
     * 只有实现 [ForkableComponent] 接口的才参与创建
     */
    @JvmOverloads
    fun fork(builder: Consumer<ProcessorComponentBuilder>? = null): DamageProcessor {
        val pcb = ProcessorComponentBuilder()

        for (component in components.values) {
            val forkable = component as? ForkableComponent<*> ?: continue
            pcb += forkable.fork()
        }

        builder?.accept(pcb)

        return DamageProcessor(pcb.build(), subscribers)
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
            subscribers: List<Subscriber>? = null,
            attackerSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(attacker),
            defenderSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(defender),
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageProcessor {
            return DamageProcessor(subscribers) { pcb ->
                pcb += Processor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot)
                pcb += TargetDefaultComponent(defender, defenderSnapshot)
                pcb += DamageMultipleComponent()
                builder?.accept(pcb)
            }
        }

        @JvmStatic
        fun ofProjectile(
            projectile: Projectile,
            defender: LivingEntity,
            shooter: Entity,
            force: Float,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return ofSnapshot(projectile, defender) { pcb ->
                pcb += ProjectileComponent(
                    TriggerComponent(shooter, AssiyaAPI.getSnapshot(shooter)),
                    pcb[TriggerComponent.Key]!!,
                    force
                )
                builder?.accept(pcb)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun ofLazyTarget(
            attacker: Entity,
            subscribers: List<Subscriber>? = null,
            attackerSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(attacker),
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageProcessor {
            return DamageProcessor(subscribers) { pcb ->
                pcb += Processor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot)
                pcb += TargetLazyComponent()
                pcb += DamageMultipleComponent()
                builder?.accept(pcb)
            }
        }

    }

}
