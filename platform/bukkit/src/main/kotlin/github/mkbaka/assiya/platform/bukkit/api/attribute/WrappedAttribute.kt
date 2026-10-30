package github.mkbaka.assiya.platform.bukkit.api.attribute

import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import org.bukkit.entity.Entity

/**
 * 供 ap 脚本迁移
 */
class WrappedAttribute(
    val attribute: Attribute
) : Attribute by attribute {

    fun isProjectile(processor: BukkitProcessor) = ProjectileComponent.Key in processor

    fun getRandomValue(entity: Entity, attribute: String, processor: BukkitProcessor): Double {
        val trigger = processor[TriggerComponent]
        val target = processor[TargetComponent]
        return when {
            trigger != null && entity.uniqueId == trigger.entity.uniqueId -> trigger.getValue(attribute, "total")

            target != null && entity.uniqueId == target.entity.uniqueId -> target.getValue(attribute, "total")

            else -> 0.0
        } ?: 0.0
    }

    fun addDamage(value: Double, processor: BukkitProcessor) {
        val multiple = processor[DamageMultipleComponent]
        if (multiple != null) {
            multiple.addDamage("vanilla", value)
            return
        }

        val single = processor[DamageComponent]
        if (single != null) {
            single.value += value
        }
    }

}
