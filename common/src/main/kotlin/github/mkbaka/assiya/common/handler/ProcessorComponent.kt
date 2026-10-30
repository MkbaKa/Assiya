package github.mkbaka.assiya.common.handler

interface ProcessorComponent {

    val key: Key<*>

    interface Key<C : ProcessorComponent>

}
