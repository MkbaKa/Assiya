package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ForkableComponent
import github.mkbaka.assiya.common.handler.ProcessorComponent

class DamageComponent(
    val id: String,
    var value: Double
) : ProcessorComponent, ForkableComponent<DamageComponent> {

    override val key: ProcessorComponent.Key<*> = Key

    override fun fork(): DamageComponent = DamageComponent(id, value)

    companion object Key : ProcessorComponent.Key<DamageComponent>

}