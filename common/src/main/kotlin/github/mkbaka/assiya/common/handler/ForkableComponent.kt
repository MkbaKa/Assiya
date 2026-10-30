package github.mkbaka.assiya.common.handler

interface ForkableComponent<T : ProcessorComponent> {

    fun fork(): T

}
