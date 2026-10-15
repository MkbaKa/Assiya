package github.mkbaka.assiya.platform.bukkit.internal.registry

import java.util.concurrent.ConcurrentHashMap

open class Registry<K : Any, V : Any> {

    protected val elements = ConcurrentHashMap<K, V>()

    val size: Int
        get() = elements.size

    protected open operator fun get(key: K): V? = elements[key]

}
