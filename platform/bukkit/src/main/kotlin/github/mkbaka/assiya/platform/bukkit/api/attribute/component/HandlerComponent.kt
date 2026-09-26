package github.mkbaka.assiya.platform.bukkit.api.attribute.component

import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerHandler

class HandlerComponent(
    val handlers: Map<Trigger, TriggerHandler>
) : AttributeComponent {

    override val key: AttributeComponent.Key<HandlerComponent> = Key

    companion object Key : AttributeComponent.Key<HandlerComponent>

}
