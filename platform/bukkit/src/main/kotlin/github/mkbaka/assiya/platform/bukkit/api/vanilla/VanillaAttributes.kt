package github.mkbaka.assiya.platform.bukkit.api.vanilla

import github.mkbaka.assiya.platform.bukkit.internal.vanilla.VanillaAttribute
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

/**
 * 非线程安全的原版属性操作包装
 */
class VanillaAttributes(val entity: LivingEntity) {

    companion object {
        @JvmStatic
        fun of(entity: LivingEntity): VanillaAttributes = VanillaAttributes(entity)
    }

    operator fun get(attribute: Attribute): VanillaAttributeView {
        requireNotNull(entity.getAttribute(attribute)) {
            "Entity ${entity.uniqueId} does not have attribute $attribute"
        }
        return VanillaAttributeView(entity, attribute)
    }

    fun find(name: String): VanillaAttributeView? {
        val attribute = VanillaAttribute[name] ?: return null
        if (entity.getAttribute(attribute) == null) return null
        return VanillaAttributeView(entity, attribute)
    }

    val maxHealth get() = get(VanillaAttribute.MAX_HEALTH)

    val followRange get() = get(VanillaAttribute.FOLLOW_RANGE)

    val knockbackResistance get() = get(VanillaAttribute.KNOCKBACK_RESISTANCE)

    val movementSpeed get() = get(VanillaAttribute.MOVEMENT_SPEED)

    val flyingSpeed get() = get(VanillaAttribute.FLYING_SPEED)

    val attackDamage get() = get(VanillaAttribute.ATTACK_DAMAGE)

    val attackKnockback get() = get(VanillaAttribute.ATTACK_KNOCKBACK)

    val attackSpeed get() = get(VanillaAttribute.ATTACK_SPEED)

    val armor get() = get(VanillaAttribute.ARMOR)

    val armorToughness get() = get(VanillaAttribute.ARMOR_TOUGHNESS)

    val fallDamageMultiplier get() = get(VanillaAttribute.FALL_DAMAGE_MULTIPLIER)

    val luck get() = get(VanillaAttribute.LUCK)

    val maxAbsorption get() = get(VanillaAttribute.MAX_ABSORPTION)

    val safeFallDistance get() = get(VanillaAttribute.SAFE_FALL_DISTANCE)

    val scale get() = get(VanillaAttribute.SCALE)

    val stepHeight get() = get(VanillaAttribute.STEP_HEIGHT)

    val gravity get() = get(VanillaAttribute.GRAVITY)

    val jumpStrength get() = get(VanillaAttribute.JUMP_STRENGTH)

    val burningTime get() = get(VanillaAttribute.BURNING_TIME)

    val cameraDistance get() = get(VanillaAttribute.CAMERA_DISTANCE)

    val explosionKnockbackResistance get() = get(VanillaAttribute.EXPLOSION_KNOCKBACK_RESISTANCE)

    val movementEfficiency get() = get(VanillaAttribute.MOVEMENT_EFFICIENCY)

    val oxygenBonus get() = get(VanillaAttribute.OXYGEN_BONUS)

    val waterMovementEfficiency get() = get(VanillaAttribute.WATER_MOVEMENT_EFFICIENCY)

    val temptRange get() = get(VanillaAttribute.TEMPT_RANGE)

    val blockInteractionRange get() = get(VanillaAttribute.BLOCK_INTERACTION_RANGE)

    val entityInteractionRange get() = get(VanillaAttribute.ENTITY_INTERACTION_RANGE)

    val blockBreakSpeed get() = get(VanillaAttribute.BLOCK_BREAK_SPEED)

    val miningEfficiency get() = get(VanillaAttribute.MINING_EFFICIENCY)

    val sneakingSpeed get() = get(VanillaAttribute.SNEAKING_SPEED)

    val submergedMiningSpeed get() = get(VanillaAttribute.SUBMERGED_MINING_SPEED)

    val sweepingDamageRatio get() = get(VanillaAttribute.SWEEPING_DAMAGE_RATIO)

    val spawnReinforcements get() = get(VanillaAttribute.SPAWN_REINFORCEMENTS)

    val waypointTransmitRange get() = get(VanillaAttribute.WAYPOINT_TRANSMIT_RANGE)

    val waypointReceiveRange get() = get(VanillaAttribute.WAYPOINT_RECEIVE_RANGE)

    val airDragModifier get() = get(VanillaAttribute.AIR_DRAG_MODIFIER)

    val frictionModifier get() = get(VanillaAttribute.FRICTION_MODIFIER)

    val bounciness get() = get(VanillaAttribute.BOUNCINESS)

    val belowNameDistance get() = get(VanillaAttribute.BELOW_NAME_DISTANCE)

    val nameTagDistance get() = get(VanillaAttribute.NAME_TAG_DISTANCE)

}
