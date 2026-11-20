package github.mkbaka.assiya.platform.bukkit.api.attribute

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.WrappedProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import org.bukkit.entity.Entity

open class WrappedAttribute(val attribute: Attribute) : Attribute by attribute {

    val attributeName: String get() = attribute[ReaderComponent]?.names?.firstOrNull() ?: id

    fun container(entity: Entity, processor: BukkitProcessor): AttributeContainer? {
        val projectile = processor[ProjectileComponent]
        if (projectile != null && projectile.shooter.entity.uniqueId == entity.uniqueId) return projectile.projectile
        val trigger = processor[TriggerComponent]
        if (trigger?.entity?.uniqueId == entity.uniqueId) return trigger
        return processor[TargetComponent]?.takeIf { it.entity.uniqueId == entity.uniqueId }
    }

    fun getRandomValue(entity: Entity, processor: BukkitProcessor): Double = getRandomValue(entity, id, processor)

    open fun getRandomValue(entity: Entity, attribute: String, processor: BukkitProcessor): Double {
        val attributeId = AssiyaAPI.getAttribute(attribute)?.id ?: attribute
        val container = requireNotNull(container(entity, processor)) { "Entity is not part of this processor" }
        return container.getValue<Number>(attributeId, "total")?.toDouble() ?: 0.0
    }

    fun addDamage(value: Double, processor: BukkitProcessor) {
        if (ScriptTools.getAttacker(processor) == null) return
        WrappedProcessor(processor).updateDamage(value)
    }

    fun isProjectile(processor: BukkitProcessor): Boolean = ProjectileComponent.Key in processor

    fun isProjectileDamage(processor: BukkitProcessor): Boolean = isProjectile(processor)

    fun isSkillDamage(processor: BukkitProcessor): Boolean = processor[TriggerByMythicSkillComponent] != null

    fun setCancelled(value: Boolean, processor: BukkitProcessor) {
        processor[CancellableComponent]?.isCancelled = value
    }

    fun chance(percent: Double): Boolean = ScriptTools.chance(percent)
}
