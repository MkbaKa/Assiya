package github.mkbaka.assiya.platform.bukkit.api.damage

import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.internal.damage.DamageMetaStack
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

/** 独立伤害任务, 在主线程调用. */
class DamageMeta(
    val processor: DamageProcessor
) {

    var ignoreImmunity = false
    var preventKnockback = false
    var noDamageTicks: Int? = null

    // 若返回 true 则取消执行
    var preDamage: Predicate<DamageMeta>? = null

    var postDamage: Consumer<DamageMeta>? = null

    fun doDamage() {
        val trigger = processor.getComponent(TriggerComponent).entity
        val target = processor.getComponent(TargetComponent).entity
        val uuid = trigger.uniqueId

        DamageMetaStack.push(uuid, this)
        try {
            processor.process()
            if (processor[CancellableComponent]?.isCancelled == true) return
            if (preDamage?.test(this) == true) return

            val block = {
                if (ignoreImmunity) {
                    target.noDamageTicks = 0
                }

                target.damage(processor.getComponent(DamageMultipleComponent).totalDamage, trigger)

                noDamageTicks?.let { target.noDamageTicks = it }
            }

            when {
                preventKnockback -> {
                    val uuid = UUID.randomUUID()
                    val modifier = AttributeModifier(
                        uuid,
                        "Assiya:damage-meta:prevent-knockback",
                        1.0,
                        AttributeModifier.Operation.ADD_NUMBER
                    )
                    val attribute = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)
                    attribute?.addModifier(modifier)
                    block()
                    attribute?.removeModifier(modifier)
                }

                else -> block()
            }

            postDamage?.accept(this)
        } finally {
            DamageMetaStack.pop(uuid)
        }
    }

    fun repeat(count: Int, periodTicks: Long, delayTicks: Long = 0) {
        var executed = 0
        var task: BukkitTask? = null
        task = SchedulerUtils.callSyncTimer(delayTicks, periodTicks) {
            if (executed++ >= count || !processor.getComponent(TargetComponent).entity.isValid) {
                task?.cancel()
                return@callSyncTimer
            }

            doDamage()
        }
    }

}
