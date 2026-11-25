package github.mkbaka.assiya.platform.bukkit.api.attribute.component

import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.api.config.MapConfigNode

class OptionsComponent(
    private val node: ConfigNode
) : AttributeComponent, ConfigNode by node {

    constructor(options: Map<String, Any?>) : this(MapConfigNode(options))

    constructor(vararg options: Pair<String, Any?>) : this(options.toMap(HashMap()))

    override val key: AttributeComponent.Key<OptionsComponent> = Key

    companion object Key : AttributeComponent.Key<OptionsComponent>

}