package github.mkbaka.assiya.platform.bukkit.api.attribute.component

import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor

@FunctionalInterface
fun interface CustomFilterComponent : AttributeComponent {

    override val key: AttributeComponent.Key<*>
        get() = Key

    /**
     * 返回 true 时才能执行
     * 反之跳过
     */
    fun test(processor: Processor): Boolean

    companion object Key : AttributeComponent.Key<CustomFilterComponent>

}
