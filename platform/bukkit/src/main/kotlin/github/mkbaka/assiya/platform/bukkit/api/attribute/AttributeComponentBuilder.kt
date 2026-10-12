package github.mkbaka.assiya.platform.bukkit.api.attribute

class AttributeComponentBuilder {

    private val components = hashMapOf<AttributeComponent.Key<*>, AttributeComponent>()

    fun add(component: AttributeComponent): AttributeComponentBuilder {
        components[component.key] = component
        return this
    }

    fun addAll(components: Iterable<AttributeComponent>): AttributeComponentBuilder {
        for (component in components) {
            add(component)
        }
        return this
    }

    fun remove(key: AttributeComponent.Key<*>): AttributeComponentBuilder {
        components.remove(key)
        return this
    }

    fun remove(component: AttributeComponent): AttributeComponentBuilder {
        return remove(component.key)
    }

    fun removeAll(components: Iterable<AttributeComponent>): AttributeComponentBuilder {
        for (component in components) {
            remove(component)
        }
        return this
    }

    operator fun plusAssign(component: AttributeComponent) {
        add(component)
    }

    operator fun plusAssign(components: Iterable<AttributeComponent>) {
        addAll(components)
    }

    operator fun plusAssign(builder: AttributeComponentBuilder) {
        addAll(builder.components.values)
    }

    operator fun minusAssign(key: AttributeComponent.Key<*>) {
        remove(key)
    }

    operator fun minusAssign(component: AttributeComponent) {
        remove(component)
    }

    operator fun minusAssign(components: Iterable<AttributeComponent>) {
        removeAll(components)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <C : AttributeComponent> get(key: AttributeComponent.Key<C>): C? {
        return components[key] as? C
    }

    operator fun <C : AttributeComponent> set(
        key: AttributeComponent.Key<C>,
        component: C
    ): AttributeComponentBuilder {
        components[key] = component
        return this
    }

    fun build() = components.toMap()

}
