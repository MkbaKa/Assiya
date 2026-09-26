package github.mkbaka.assiya.platform.bukkit.api.handler

interface ProcessorComponent {

    val key: Key<*>

    interface Key<C : ProcessorComponent>

}