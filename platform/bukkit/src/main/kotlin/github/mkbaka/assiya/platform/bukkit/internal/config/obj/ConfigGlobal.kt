package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode

class ConfigGlobal(
    section: ConfigNode
) {

    val EQUIPMENT_REFRESH_DELAY_TICKS = section.getLong("equipment.refresh-delay-ticks", 8L)

    val EQUIPMENT_SOURCE_PRIORITY = section.getInt("equipment.source-priority", 100)

    val PARSER_CACHE_MAXIMUM_SIZE = section.getLong("parser.cache.maximum-size", 10_000L)

    val PARSER_CACHE_EXPIRE_AFTER_ACCESS_SECONDS = section.getLong("parser.cache.expire-after-access-seconds", 600L)

    val MYTHICMOBS_SOURCE_PRIORITY = section.getInt("mythicmobs.source-priority", 100)

    val ASYNC_THREADS = section.getInt("async.threads", 0)

    val ROMAN_VALUES: Map<Char, Int> = buildMap {
        val roman = section.getNode("formats.roman")!!
        for (key in roman.keys()) {
            put(key.single(), roman.getInt(key))
        }
    }

}
