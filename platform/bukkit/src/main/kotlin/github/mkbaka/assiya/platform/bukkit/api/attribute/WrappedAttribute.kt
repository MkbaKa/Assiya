package github.mkbaka.assiya.platform.bukkit.api.attribute

import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import org.bukkit.entity.Entity

/**
 * 供 ap 脚本迁移
 */
class WrappedAttribute(
    val attribute: Attribute
) : Attribute by attribute {

    fun isProjectile(processor: Processor) = ProjectileComponent.Key in processor

    fun getRandomValue(entity: Entity, attribute: String, processor: Processor): Double {
        val trigger = processor[TriggerComponent.Key]
        val target = processor[TargetComponent.Key]
        return when {
            trigger != null && entity.uniqueId == trigger.entity.uniqueId -> trigger.getValue(attribute, "total")

            target != null && entity.uniqueId == target.entity.uniqueId -> target.getValue(attribute, "total")

            else -> 0.0
        } ?: 0.0
    }

    fun addDamage(value: Double, processor: Processor) {
        val multiple = processor[DamageMultipleComponent.Key]
        if (multiple != null) {
            multiple.addDamage("vanilla", value)
            return
        }

        val single = processor[DamageComponent.Key]
        if (single != null) {
            single.value += value
        }
    }

}
