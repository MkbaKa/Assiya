package github.mkbaka.assiya.platform.bukkit.api.condition.component

import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionComponent
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode

class OptionsComponent(private val node: ConfigNode) : ConditionComponent, ConfigNode by node {
    override val key: ConditionComponent.Key<OptionsComponent> = Key

    companion object Key : ConditionComponent.Key<OptionsComponent>
}
