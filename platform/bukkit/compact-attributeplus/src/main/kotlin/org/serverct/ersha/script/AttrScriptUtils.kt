package org.serverct.ersha.script

import github.mkbaka.assiya.common.formula.FormulaCompiler
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.CooldownAPI
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.hook.placeholderapi.AssiyaPlaceholderAPIHook.parsePAPI
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.serverct.ersha.api.AttributeAPI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object AttrScriptUtils {

    @JvmStatic
    fun getAttrData(entity: LivingEntity) = AttributeAPI.getAttrData(entity)

    @JvmStatic
    fun registerOtherAttribute(name: String, combatPower: Double, placeholder: String) {
        APScriptLoader.registerOther(name, combatPower, placeholder)
    }

    @JvmStatic
    fun getNearbyEntities(entity: LivingEntity, x: Double, y: Double, z: Double, self: Boolean): List<Entity> =
        getNearbyEntities(entity, x, y, z, self, arrayListOf())

    @JvmStatic
    fun getNearbyEntities(
        entity: LivingEntity,
        x: Double,
        y: Double,
        z: Double,
        self: Boolean,
        types: List<EntityType>
    ): List<Entity> {
        @Suppress("UNCHECKED_CAST")
        val entities = entity.getNearbyEntities(x, y, z)
            .filter { if (types.isNotEmpty()) it.type in types else it is LivingEntity }
            .toMutableList()

        if (!self) entities.remove(entity)
        return entities
    }

    @JvmStatic
    fun damageEntity(damage: Double, entity: LivingEntity, killer: LivingEntity) {
        BukkitCoroutine.launch(entity) {
            if (entity.isValid) {
                // 带来源伤害仍要求双方同区域, 不在这里重放跨区域伤害.
                if (FoliaCompat.isFolia() && !FoliaCompat.isOwnedByCurrentRegion(killer)) return@launch
                AssiyaAPI.attackTo(killer, entity, damage)
            }
        }
    }

    @JvmStatic
    fun damageEntity(damage: Double, entity: LivingEntity) {
        BukkitCoroutine.launch(entity) {
            if (entity.isValid) {
                entity.damage(damage)
            }
        }
    }

    @JvmStatic
    fun isType(entity: LivingEntity, type: List<EntityType>): Boolean = type.contains(entity.type)

    @Deprecated("calculatorPlaceholder(player, expr)")
    @JvmStatic
    fun evalPlaceholderValue(player: LivingEntity, plan: String): Double = calculatorPlaceholder(player, plan)

    @JvmStatic
    fun calculator(player: LivingEntity, expr: String): Double {
        if (player !is Player) return 0.0

        val formula = FormulaCompiler.compile("attrscriptutils:$expr", expr)
        val result = formula.invoke { null }
        return result
    }

    @JvmStatic
    fun calculatorPlaceholder(player: LivingEntity, expr: String): Double {
        if (player !is Player) return 0.0
        return calculator(player, expr.parsePAPI(player))
    }

    @JvmStatic
    fun placeholder(player: LivingEntity, context: String): String {
        if (player !is Player) return context
        return context.parsePAPI(player)
    }

    @JvmStatic
    fun placeholderToDouble(player: LivingEntity, context: String, default: Double): Double =
        if (player is Player) placeholder(player, context).toDoubleOrNull() ?: default else default

    @JvmStatic
    fun attributeCooling(key: String, time: Int, entity: LivingEntity): Boolean {
        val session = AssiyaAPI.getSession(entity) ?: return false
        return CooldownAPI.tryAcquire(
            session,
            "attributeplus:attribute-cooldown:$key",
            time.seconds.coerceAtLeast(Duration.ZERO)
        )
    }

    @JvmStatic
    fun hasCooling(key: String, entity: LivingEntity): Boolean =
        AssiyaAPI.getSession(entity)?.let {
            CooldownAPI.isExpired(it, "attributeplus:cooldown:$key")
        } ?: true

    @JvmStatic
    fun hasCooling(key: String, entity: LivingEntity, seconds: Int): Boolean =
        hasCooling(key, entity, seconds.toDouble())

    @JvmStatic
    fun hasCooling(key: String, entity: LivingEntity, seconds: Double): Boolean {
        val session = AssiyaAPI.getSession(entity) ?: return false
        return CooldownAPI.tryAcquire(
            session,
            "attributeplus:cooldown:$key",
            seconds.seconds.coerceAtLeast(Duration.ZERO)
        )
    }

    @JvmStatic
    fun resetCooling(key: String, entity: LivingEntity) {
        val session = AssiyaAPI.getSession(entity) ?: return
        CooldownAPI.remove(session, "attributeplus:cooldown:$key")
    }

    @JvmStatic
    fun hasEntityCooling(name: String, entity: LivingEntity): Boolean =
        AssiyaAPI.getSession(entity)?.let {
            CooldownAPI.isExpired(it, "attributeplus:entity-cooldown:$name")
        } ?: true

    @JvmStatic
    fun hasEntityCooling(name: String, entity: LivingEntity, time: Int): Boolean =
        hasEntityCooling(name, entity, time.toDouble())

    @JvmStatic
    fun hasEntityCooling(name: String, entity: LivingEntity, time: Double): Boolean {
        val session = AssiyaAPI.getSession(entity) ?: return false
        return CooldownAPI.tryAcquire(
            session,
            "attributeplus:entity-cooldown:$name",
            time.seconds.coerceAtLeast(Duration.ZERO)
        )
    }

    @JvmStatic
    fun resetEntityCooling(name: String, entity: LivingEntity) {
        val session = AssiyaAPI.getSession(entity) ?: return
        CooldownAPI.remove(session, "attributeplus:entity-cooldown:$name")
    }

    @JvmStatic
    fun safeHeal(entity: LivingEntity, heal: Double) {
        BukkitCoroutine.launch(entity) {
            AssiyaAPI.heal(entity, heal)
        }
    }

    @JvmStatic
    fun addPotionEffect(entity: LivingEntity, type: PotionEffectType, level: Int, time: Int) {
        BukkitCoroutine.launch(entity) {
            entity.addPotionEffect(PotionEffect(type, time, level))
        }
    }

}
