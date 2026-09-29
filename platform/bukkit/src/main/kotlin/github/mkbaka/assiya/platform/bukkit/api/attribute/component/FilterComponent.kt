package github.mkbaka.assiya.platform.bukkit.api.attribute.component

import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor


fun interface FilterComponent : AttributeComponent {

    override val key: AttributeComponent.Key<*>
        get() = Key

    fun test(processor: Processor): Boolean

    companion object Key : AttributeComponent.Key<FilterComponent>

}