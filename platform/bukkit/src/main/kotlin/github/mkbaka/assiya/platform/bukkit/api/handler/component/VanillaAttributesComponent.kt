package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes

class VanillaAttributesComponent(
    vanillaAttributes: VanillaAttributes
) : ProcessorComponent, VanillaAttributes(vanillaAttributes.entity) {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<VanillaAttributesComponent>

}