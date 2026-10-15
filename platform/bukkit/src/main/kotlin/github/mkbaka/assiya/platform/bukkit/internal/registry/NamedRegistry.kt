package github.mkbaka.assiya.platform.bukkit.internal.registry

open class NamedRegistry<V : Any>(
    private val elementName: String,
) : Registry<String, V>() {

    val values: Collection<V>
        get() = elements.values

    public override operator fun get(key: String): V? = elements[key.lowercase()]

    open fun register(id: String, value: V) {
        require(elements.putIfAbsent(id.lowercase(), value) == null) {
            "$elementName $id 已被注册"
        }
    }

    open fun unregister(id: String, value: V): Boolean {
        return elements.remove(id.lowercase(), value)
    }
}
