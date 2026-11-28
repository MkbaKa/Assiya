@file:Suppress("DEPRECATION")

package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class TriggerByEventComponent @JvmOverloads constructor(
    val event: EntityDamageByEntityEvent,
    val force: Float = calculateForce(event)
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    val isBlocking: Boolean
        get() = event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) > 0

    companion object Key : ProcessorComponent.Key<TriggerByEventComponent> {

        private fun calculateForce(event: EntityDamageByEntityEvent): Float {
            val attacker = event.damager as? LivingEntity ?: return 1.0f
            val originalDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE)
            if (originalDamage <= 0.0) return 1.0f

            val attackDamage = VanillaAttributes.of(attacker).find("ATTACK_DAMAGE")?.value ?: return 1.0f
            if (attackDamage == 0.0) return 1.0f

            val force = originalDamage / attackDamage
            return if (force.isFinite()) force.toFloat() else 1.0f
        }

    }

}
