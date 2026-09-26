package github.mkbaka.assiya.platform.bukkit.internal.registry

import java.util.concurrent.ConcurrentHashMap

open class Registry<T : Any>(
    private val elementName: String,
) {

    private val elements = ConcurrentHashMap<String, T>()

    val values: Collection<T>
        get() = elements.values

    val size: Int
        get() = elements.size

    operator fun get(id: String): T? = elements[id.lowercase()]

    open fun register(id: String, value: T) {
        require(elements.putIfAbsent(id.lowercase(), value) == null) {
            "$elementName $id 已被注册"
        }
    }

    open fun unregister(id: String, value: T): Boolean {
        return elements.remove(id.lowercase(), value)
    }

}
