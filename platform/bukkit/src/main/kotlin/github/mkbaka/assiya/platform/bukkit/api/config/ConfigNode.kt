package github.mkbaka.assiya.platform.bukkit.api.config

import org.bukkit.configuration.ConfigurationSection

interface ConfigNode {

    fun keys(): Set<String>

    fun get(key: String): Any?

    fun getNode(key: String): ConfigNode? = of(get(key))

    fun getString(key: String, def: String? = null): String? {
        return get(key)?.toString() ?: def
    }

    fun getInt(key: String, def: Int): Int {
        val value = get(key) ?: return def
        if (value is Number) return value.toInt()
        return value.toString().toIntOrNull()
            ?: throw NumberFormatException("node $key is not a number: $value")
    }

    fun getDouble(key: String, def: Double): Double {
        val value = get(key) ?: return def
        if (value is Number) return value.toDouble()
        return value.toString().toDoubleOrNull()
            ?: throw NumberFormatException("node $key is not a number: $value")
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        val value = get(key) ?: return def
        if (value is Boolean) return value
        return value.toString().toBooleanStrictOrNull()
            ?: throw IllegalArgumentException("node $key is not a boolean: $value")
    }

    fun getStrings(key: String): List<String> {
        val value = get(key) ?: return emptyList()
        if (value is String) return listOf(value)
        if (value is List<*>) return value.mapNotNull { it?.toString() }
        throw IllegalArgumentException("node $key is not a string: $value")
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun of(obj: Any?): ConfigNode? {
            return when (obj) {
                is ConfigNode -> obj
                is ConfigurationSection -> BukkitConfigNode(obj)
                is Map<*, *> -> MapConfigNode(obj as Map<String, Any?>)
                else -> null
            }
        }

    }

}