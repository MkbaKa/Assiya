package github.mkbaka.assiya.platform.bukkit.api.handler

interface ForkableComponent<T : ProcessorComponent> {

    fun fork(): T

}
