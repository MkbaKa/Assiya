package github.mkbaka.assiya.platform.bukkit.api.attribute.component

import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode

class OptionsComponent(
    private val node: ConfigNode
) : AttributeComponent, ConfigNode by node {

    override val key: AttributeComponent.Key<OptionsComponent> = Key

    companion object Key : AttributeComponent.Key<OptionsComponent>

}