package github.mkbaka.assiya.platform.bukkit.internal.vanilla

import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import java.lang.reflect.Modifier
import java.util.*

object VanillaAttribute {

    /**
     * 生命上限
     */
    @JvmStatic
    val MAX_HEALTH: Attribute get() = require("MAX_HEALTH", "1.9")

    /**
     * 生物追踪目标的范围
     */
    @JvmStatic
    val FOLLOW_RANGE: Attribute get() = require("FOLLOW_RANGE", "1.9")

    /**
     * 击退抗性
     */
    @JvmStatic
    val KNOCKBACK_RESISTANCE: Attribute get() = require("KNOCKBACK_RESISTANCE", "1.9")

    /**
     * 移动速度
     */
    @JvmStatic
    val MOVEMENT_SPEED: Attribute get() = require("MOVEMENT_SPEED", "1.9")

    /**
     * 飞行速度
     */
    @JvmStatic
    val FLYING_SPEED: Attribute get() = require("FLYING_SPEED", "1.12")

    /**
     * 攻击伤害
     */
    @JvmStatic
    val ATTACK_DAMAGE: Attribute get() = require("ATTACK_DAMAGE", "1.9")

    /**
     * 攻击造成的击退强度
     */
    @JvmStatic
    val ATTACK_KNOCKBACK: Attribute get() = require("ATTACK_KNOCKBACK", "1.16.1")

    /**
     * 攻击速度
     */
    @JvmStatic
    val ATTACK_SPEED: Attribute get() = require("ATTACK_SPEED", "1.9")

    /**
     * 护甲值
     */
    @JvmStatic
    val ARMOR: Attribute get() = require("ARMOR", "1.9")

    /**
     * 护甲韧性
     */
    @JvmStatic
    val ARMOR_TOUGHNESS: Attribute get() = require("ARMOR_TOUGHNESS", "1.11.2")

    /**
     * 摔落伤害倍率
     */
    @JvmStatic
    val FALL_DAMAGE_MULTIPLIER: Attribute get() = require("FALL_DAMAGE_MULTIPLIER", "1.20.5")

    /**
     * 幸运值
     */
    @JvmStatic
    val LUCK: Attribute get() = require("LUCK", "1.9")

    /**
     * 伤害吸收生命值上限
     */
    @JvmStatic
    val MAX_ABSORPTION: Attribute get() = require("MAX_ABSORPTION", "1.20.2")

    /**
     * 不受摔落伤害的安全下落距离
     */
    @JvmStatic
    val SAFE_FALL_DISTANCE: Attribute get() = require("SAFE_FALL_DISTANCE", "1.20.5")

    /**
     * 实体体型缩放比例
     */
    @JvmStatic
    val SCALE: Attribute get() = require("SCALE", "1.20.5")

    /**
     * 无需跳跃即可迈上的高度
     */
    @JvmStatic
    val STEP_HEIGHT: Attribute get() = require("STEP_HEIGHT", "1.20.5")

    /**
     * 实体受到的重力
     */
    @JvmStatic
    val GRAVITY: Attribute get() = require("GRAVITY", "1.20.5")

    /**
     * 跳跃强度
     */
    @JvmStatic
    val JUMP_STRENGTH: Attribute get() = require("JUMP_STRENGTH", "1.9")

    /**
     * 被点燃后的燃烧时长
     */
    @JvmStatic
    val BURNING_TIME: Attribute get() = require("BURNING_TIME", "1.21")

    /**
     * 玩家第三人称视角的相机距离
     */
    @JvmStatic
    val CAMERA_DISTANCE: Attribute get() = require("CAMERA_DISTANCE", "1.21.6")

    /**
     * 爆炸击退抗性
     */
    @JvmStatic
    val EXPLOSION_KNOCKBACK_RESISTANCE: Attribute get() = require("EXPLOSION_KNOCKBACK_RESISTANCE", "1.21")

    /**
     * 在减速地形上的移动效率
     */
    @JvmStatic
    val MOVEMENT_EFFICIENCY: Attribute get() = require("MOVEMENT_EFFICIENCY", "1.21")

    /**
     * 水下呼吸时减少氧气消耗的能力
     */
    @JvmStatic
    val OXYGEN_BONUS: Attribute get() = require("OXYGEN_BONUS", "1.21")

    /**
     * 水中的移动效率
     */
    @JvmStatic
    val WATER_MOVEMENT_EFFICIENCY: Attribute get() = require("WATER_MOVEMENT_EFFICIENCY", "1.21")

    /**
     * 生物被物品吸引的范围
     */
    @JvmStatic
    val TEMPT_RANGE: Attribute get() = require("TEMPT_RANGE", "1.21.2")

    /**
     * 玩家与方块交互的距离
     */
    @JvmStatic
    val BLOCK_INTERACTION_RANGE: Attribute get() = require("BLOCK_INTERACTION_RANGE", "1.20.5")

    /**
     * 玩家与实体交互的距离
     */
    @JvmStatic
    val ENTITY_INTERACTION_RANGE: Attribute get() = require("ENTITY_INTERACTION_RANGE", "1.20.5")

    /**
     * 方块破坏速度
     */
    @JvmStatic
    val BLOCK_BREAK_SPEED: Attribute get() = require("BLOCK_BREAK_SPEED", "1.20.5")

    /**
     * 使用合适工具时的挖掘效率
     */
    @JvmStatic
    val MINING_EFFICIENCY: Attribute get() = require("MINING_EFFICIENCY", "1.21")

    /**
     * 潜行速度
     */
    @JvmStatic
    val SNEAKING_SPEED: Attribute get() = require("SNEAKING_SPEED", "1.21")

    /**
     * 水下挖掘速度
     */
    @JvmStatic
    val SUBMERGED_MINING_SPEED: Attribute get() = require("SUBMERGED_MINING_SPEED", "1.21")

    /**
     * 横扫攻击的伤害比例
     */
    @JvmStatic
    val SWEEPING_DAMAGE_RATIO: Attribute get() = require("SWEEPING_DAMAGE_RATIO", "1.21")

    /**
     * 僵尸生成增援的概率
     */
    @JvmStatic
    val SPAWN_REINFORCEMENTS: Attribute get() = require("SPAWN_REINFORCEMENTS", "1.9")

    /**
     * 实体向定位栏广播自身位置的范围
     */
    @JvmStatic
    val WAYPOINT_TRANSMIT_RANGE: Attribute get() = require("WAYPOINT_TRANSMIT_RANGE", "1.21.6")

    /**
     * 实体接收其他定位点的范围
     */
    @JvmStatic
    val WAYPOINT_RECEIVE_RANGE: Attribute get() = require("WAYPOINT_RECEIVE_RANGE", "1.21.6")

    /**
     * 空气阻力修正
     */
    @JvmStatic
    val AIR_DRAG_MODIFIER: Attribute get() = require("AIR_DRAG_MODIFIER", "26.2")

    /**
     * 地面摩擦力修正
     */
    @JvmStatic
    val FRICTION_MODIFIER: Attribute get() = require("FRICTION_MODIFIER", "26.2")

    /**
     * 落地时的弹跳强度
     */
    @JvmStatic
    val BOUNCINESS: Attribute get() = require("BOUNCINESS", "26.2")

    /**
     * 名称下方计分板分数的显示距离阈值
     */
    @JvmStatic
    val BELOW_NAME_DISTANCE: Attribute get() = require("BELOW_NAME_DISTANCE", "26.2")

    /**
     * 实体名称的显示距离阈值
     */
    @JvmStatic
    val NAME_TAG_DISTANCE: Attribute get() = require("NAME_TAG_DISTANCE", "26.2")

    @JvmStatic
    val available: Map<String, Attribute> by lazy {
        val attributes = hashMapOf<String, Attribute>()

        val type = Attribute::class.java
        for (field in type.fields) {
            if (!Modifier.isStatic(field.modifiers)
                || !type.isAssignableFrom(field.type)
            ) continue

            // 去掉老版本叽里咕噜的前缀
            val name = normalize(field.name)
            val attribute = type.cast(field.get(null))

            attributes[name] = attribute
        }

        Collections.unmodifiableMap(attributes)
    }

    @JvmStatic
    operator fun get(name: String): Attribute? = available[normalize(name)]

    private fun require(name: String, since: String): Attribute =
        get(name) ?: error(
            "Attribute $name require Minecraft $since or later, current server version: ${Bukkit.getBukkitVersion()}"
        )

    private fun normalize(name: String): String {
        val upperName = name.uppercase(Locale.ROOT)
        for (prefix in arrayOf("GENERIC_", "PLAYER_", "ZOMBIE_", "HORSE_")) {
            if (upperName.startsWith(prefix)) return upperName.removePrefix(prefix)
        }
        return upperName
    }

}
