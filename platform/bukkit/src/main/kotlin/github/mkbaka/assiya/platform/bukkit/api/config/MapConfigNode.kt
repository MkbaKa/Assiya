package github.mkbaka.assiya.platform.bukkit.api.config

class MapConfigNode(
    private val handle: Map<String, Any?>
) : ConfigNode {

    override fun keys(): Set<String> = handle.keys

    override fun get(key: String): Any? {
        var current: Map<*, *>? = handle
        var value: Any? = null

        for (part in key.split('.')) {
            val map = current ?: return null
            if (!map.containsKey(part)) return null
            value = map[part]
            current = value as? Map<*, *>
        }

        return value
    }

}