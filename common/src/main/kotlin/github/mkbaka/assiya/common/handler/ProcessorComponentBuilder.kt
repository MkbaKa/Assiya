package github.mkbaka.assiya.common.handler

class ProcessorComponentBuilder {

    private val components = hashMapOf<ProcessorComponent.Key<*>, ProcessorComponent>()

    fun add(component: ProcessorComponent): ProcessorComponentBuilder {
        components[component.key] = component
        return this
    }

    fun addAll(components: Iterable<ProcessorComponent>): ProcessorComponentBuilder {
        for (component in components) {
            add(component)
        }
        return this
    }

    fun remove(key: ProcessorComponent.Key<*>): ProcessorComponentBuilder {
        components.remove(key)
        return this
    }

    fun remove(component: ProcessorComponent): ProcessorComponentBuilder {
        return remove(component.key)
    }

    fun removeAll(components: Iterable<ProcessorComponent>): ProcessorComponentBuilder {
        for (component in components) {
            remove(component)
        }
        return this
    }

    operator fun plusAssign(component: ProcessorComponent) {
        add(component)
    }

    operator fun plusAssign(components: Iterable<ProcessorComponent>) {
        addAll(components)
    }

    operator fun plusAssign(pcb: ProcessorComponentBuilder) {
        addAll(pcb.components.values)
    }

    operator fun minusAssign(key: ProcessorComponent.Key<*>) {
        remove(key)
    }

    operator fun minusAssign(component: ProcessorComponent) {
        remove(component)
    }

    operator fun minusAssign(components: Iterable<ProcessorComponent>) {
        removeAll(components)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <C : ProcessorComponent> get(key: ProcessorComponent.Key<C>): C? {
        return components[key] as? C
    }

    operator fun <C : ProcessorComponent> set(
        key: ProcessorComponent.Key<C>,
        component: C
    ): ProcessorComponentBuilder {
        components[key] = component
        return this
    }

    fun build() = components.toMap()

}
