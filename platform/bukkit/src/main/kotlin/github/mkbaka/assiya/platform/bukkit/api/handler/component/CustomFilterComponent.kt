package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry.Subscriber

@FunctionalInterface
fun interface CustomFilterComponent : ProcessorComponent {

    override val key: ProcessorComponent.Key<*>
        get() = Key

    /**
     * 返回 true 时才能执行
     * 反之跳过
     */
    fun test(processor: Processor, subscriber: Subscriber): Boolean

    companion object Key : ProcessorComponent.Key<CustomFilterComponent>

}
