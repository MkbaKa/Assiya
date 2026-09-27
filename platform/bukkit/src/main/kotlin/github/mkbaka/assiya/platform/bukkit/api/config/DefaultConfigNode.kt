package github.mkbaka.assiya.platform.bukkit.api.config

class DefaultConfigNode(
    private val content: ConfigNode,
    private val defaults: ConfigNode
) : ConfigNode {

    override fun keys(): Set<String> = content.keys() + defaults.keys()

    override fun get(key: String): Any? {
        val separator = key.indexOf('.')
        if (separator >= 0) {
            val head = key.substring(0, separator)
            val value = get(head) ?: return null
            val child = ConfigNode.of(value)
                ?: throw IllegalArgumentException("node $head is not a configuration section: $value")
            return child.get(key.substring(separator + 1))
        }
        if (!content.contains(key)) return defaults.get(key)
        val value = content.get(key)
        val child = ConfigNode.of(value) ?: return value
        val fallback = defaults.getNode(key) ?: return value
        return DefaultConfigNode(child, fallback)
    }

}
