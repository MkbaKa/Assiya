package github.mkbaka.assiya.common.handler

interface Processor {

    val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>

    fun <C : ProcessorComponent> getComponent(key: ProcessorComponent.Key<C>): C {
        return get(key)!!
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <C : ProcessorComponent> get(key: ProcessorComponent.Key<C>): C? {
        return components[key] as? C
    }

    operator fun contains(key: ProcessorComponent.Key<*>): Boolean = key in components

    fun process()

}
