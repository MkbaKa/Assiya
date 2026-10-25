package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode

class ConfigGlobal(
    private val section: ConfigNode
) {

    val VANILLA_DAMAGE = section.getBoolean("settings.vanilla-damage", true)

    val HEALTH_SCALE = section.getBoolean("settings.health-scale", true)

    val HEALTH_SCALE_VALUE = section.getDouble("settings.health-scale-value", 40.0)

    val PLAYER_SESSION_LIFECYCLE = section.getBoolean("settings.player-session-lifecycle", true)

    val PLAYER_SESSION_TIMEOUT = section.getLong("settings.player-session-timeout", 600L)

    val COMBAT_DURATION = section.getDouble("settings.combat-duration", 10.0)

    val TIMER_RESET_PERIOD = section.getLong("settings.timer-reset-period", 1200L).also {
        require(it > 0) { "settings.timer-reset-period must be positive" }
    }

    val EQUIPMENT_REFRESH_DELAY_TICKS = section.getLong("equipment.refresh-delay-ticks", 8L)

    val EQUIPMENT_SOURCE_PRIORITY = section.getInt("equipment.source-priority", 100)

    val PARSER_CACHE_MAXIMUM_SIZE = section.getLong("parser.cache.maximum-size", 10_000L)

    val PARSER_CACHE_EXPIRE_AFTER_ACCESS_SECONDS = section.getLong("parser.cache.expire-after-access-seconds", 600L)

    val MYTHICMOBS_SOURCE_PRIORITY = section.getInt("mythicmobs.source-priority", 100)

    val ASYNC_THREADS = section.getInt("async.threads", 0)

    val DEFAULT_ATTRIBUTES_PRIORITY = section.getInt("default-attributes.priority", 80)

    var DEFAULT_ATTRIBUTES_PLAYER: AttributeParsedSource? = null
        private set

    var DEFAULT_ATTRIBUTES_ENTITY: AttributeParsedSource? = null
        private set

    val ROMAN_VALUES: Map<Char, Int> = buildMap {
        val roman = section.getNode("formats.roman")!!
        for (key in roman.keys()) {
            put(key.single(), roman.getInt(key))
        }
    }

    /**
     * config 加载在属性匹配规则的编译之前
     *  这个提前解析要放在属性规则编译后调用
     */
    fun parseDefaultAttributes() {
        DEFAULT_ATTRIBUTES_PLAYER = section.getStringList("default-attributes.player")
            .takeIf { it.isNotEmpty() }?.let(AssiyaAPI::parse)
        DEFAULT_ATTRIBUTES_ENTITY = section.getStringList("default-attributes.entity")
            .takeIf { it.isNotEmpty() }?.let(AssiyaAPI::parse)
    }

}
