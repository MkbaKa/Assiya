package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent

class DamageComponent(
    val id: String,
    var value: Double
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<DamageComponent>

}