package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable

open class WrappedProcessor(val processor: BukkitProcessor) : BukkitProcessor by processor, Cancellable {

    val attackerOrKiller: Entity? get() = ScriptTools.getAttacker(processor)

    val entity: Entity? get() = processor[TargetComponent]?.entity ?: processor[TriggerComponent]?.entity

    val isProjectile: Boolean get() = processor[ProjectileComponent] != null

    val isSkillDamage: Boolean get() = processor[TriggerByMythicSkillComponent] != null

    fun getDamage(): Double =
        processor[DamageMultipleComponent]?.totalDamage ?: processor[DamageComponent]?.value ?: 0.0

    fun setDamage(value: Double) {
        if (value.isNaN()) return
        val multiple = processor[DamageMultipleComponent]
        if (multiple != null) {
            multiple.clear()
            multiple.setDamage("vanilla", value)
        } else {
            processor[DamageComponent]?.value = value
        }
    }

    fun updateDamage(value: Double) {
        if (value.isNaN()) return
        val next = (getDamage() + value).coerceAtLeast(0.0)
        val multiple = processor[DamageMultipleComponent]
        if (multiple != null) {
            multiple.addDamage("assiya:adjustment", next - multiple.totalDamage)
        } else {
            processor[DamageComponent]?.value = next
        }
    }

    override fun isCancelled(): Boolean = processor[CancellableComponent]?.isCancelled == true

    override fun setCancelled(cancelled: Boolean) {
        processor[CancellableComponent]?.isCancelled = cancelled
    }
}
