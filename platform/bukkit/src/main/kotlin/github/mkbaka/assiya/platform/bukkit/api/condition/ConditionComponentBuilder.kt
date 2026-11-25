package github.mkbaka.assiya.platform.bukkit.api.condition

class ConditionComponentBuilder {

    private val components = hashMapOf<ConditionComponent.Key<*>, ConditionComponent>()

    fun add(component: ConditionComponent): ConditionComponentBuilder {
        components[component.key] = component
        return this
    }

    fun addAll(components: Iterable<ConditionComponent>): ConditionComponentBuilder {
        for (component in components) {
            add(component)
        }
        return this
    }

    fun remove(key: ConditionComponent.Key<*>): ConditionComponentBuilder {
        components.remove(key)
        return this
    }

    fun remove(component: ConditionComponent): ConditionComponentBuilder = remove(component.key)

    fun removeAll(components: Iterable<ConditionComponent>): ConditionComponentBuilder {
        for (component in components) {
            remove(component)
        }
        return this
    }

    operator fun plusAssign(component: ConditionComponent) { add(component) }

    operator fun plusAssign(components: Iterable<ConditionComponent>) { addAll(components) }

    operator fun plusAssign(builder: ConditionComponentBuilder) { addAll(builder.components.values) }

    operator fun minusAssign(key: ConditionComponent.Key<*>) { remove(key) }

    operator fun minusAssign(component: ConditionComponent) { remove(component) }

    operator fun minusAssign(components: Iterable<ConditionComponent>) { removeAll(components) }

    @Suppress("UNCHECKED_CAST")
    operator fun <C : ConditionComponent> get(key: ConditionComponent.Key<C>): C? = components[key] as? C

    operator fun <C : ConditionComponent> set(key: ConditionComponent.Key<C>, component: C): ConditionComponentBuilder {
        components[key] = component
        return this
    }

    fun build() = components.toMap()

}
