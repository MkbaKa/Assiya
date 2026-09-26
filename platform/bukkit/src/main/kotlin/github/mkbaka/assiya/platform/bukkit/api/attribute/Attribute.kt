package github.mkbaka.assiya.platform.bukkit.api.attribute

import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration

interface Attribute {

    val id: String

    val components: Map<AttributeComponent.Key<*>, AttributeComponent>

    @Suppress("UNCHECKED_CAST")
    operator fun <C : AttributeComponent> get(key: AttributeComponent.Key<C>): C? {
        return components[key] as? C
    }

    fun register() {
        AttributeRegistration.register(this)
    }

    fun unregister() {
        AttributeRegistration.unregister(this)
    }

}
