package github.mkbaka.assiya.platform.bukkit.api.attribute

interface AttributeComponent {

    val key: Key<*>

    interface Key<C : AttributeComponent>

}