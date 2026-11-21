package github.mkbaka.assiya.platform.bukkit.api.damage

import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.event.DamageMetaEvent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * 唤起一次经 Assiya 处理的伤害
 *
 * @param [processor] 上下文
 * @param [process] 是否执行属性效果
 */
class DamageMeta @JvmOverloads constructor(
    val processor: DamageProcessor,
    val process: Boolean = true
) {

    constructor(process: Boolean = true, factor: () -> DamageProcessor) : this(factor(), process)

    var ignoreImmunity = false
    var preventKnockback = false
    var noDamageTicks: Int? = null

    // 若返回 true 则取消执行
    var preDamage: Predicate<DamageMeta>? = null

    var postDamage: Consumer<DamageMeta>? = null

    fun doDamage() {
        execute {
            if (process) {
                if (DamageMetaEvent.PreProcess(this, processor).call()) {
                    return@execute
                }

                processor.process()

                DamageMetaEvent.PostProcess(this, processor).call()
            }
        }
    }

    private fun execute(prepare: () -> Unit) {
        if (DamageMetaEvent.Start(this, processor).call()) {
            return
        }

        val trigger = processor.getComponent(TriggerComponent)
        val target = processor.getComponent(TargetComponent).entity

        val damageMetaStack = trigger.session?.damageMetaStack
        val damageProcessorStack = trigger.session?.damageProcessorStack
        damageMetaStack?.push(this)
        damageProcessorStack?.push(processor)

        try {
            prepare()

            if (DamageMetaEvent.PreDamage(this, processor).call()) {
                return
            }

            if (processor[CancellableComponent]?.isCancelled == true) return
            if (preDamage?.test(this) == true) return

            val block = {
                if (ignoreImmunity) {
                    target.noDamageTicks = 0
                }

                target.damage(processor.getComponent(DamageMultipleComponent).totalDamage, trigger.entity)

                noDamageTicks?.let { target.noDamageTicks = it }
            }

            when {
                preventKnockback -> {
                    val knockbackResistance = VanillaAttributes.of(target).knockbackResistance
                    val source = "Assiya:damage-meta:prevent-knockback"
                    knockbackResistance.setTransient(source, 1.0, AttributeModifier.Operation.ADD_NUMBER)
                    block()
                    knockbackResistance.removeModifier(source)
                }

                else -> block()
            }

            DamageMetaEvent.PostDamage(this, processor).call()
            postDamage?.accept(this)
        } finally {
            damageProcessorStack?.pop()
            damageMetaStack?.pop()
        }

        DamageMetaEvent.End(this, processor).call()
    }

    companion object {

        /**
         * 构造一个固定伤害的 DamageMeta 不会再触发属性逻辑
         * 等价于 [LivingEntity.damage]
         * 但经过 Assiya 包装
         *
         * @param [attacker]
         * @param [defender]
         * @param [damage]
         * @return [DamageMeta]
         */
        @JvmOverloads
        @JvmStatic
        fun fixedDamage(
            attacker: Entity,
            defender: LivingEntity,
            damage: Double,
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageMeta {
            return DamageMeta(process = false) {
                DamageProcessor.of(attacker, defender) { pcb ->
                    pcb[DamageMultipleComponent]!!.setDamage("vanilla", damage)
                    builder?.accept(pcb)
                }
            }
        }

        fun lazyTarget(
            attacker: Entity,
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageMeta {
            return DamageMeta {
                DamageProcessor.ofLazyTarget(attacker, builder = builder)
            }
        }

    }

}
