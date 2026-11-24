package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.HealMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import groovy.transform.CompileStatic

@CompileStatic
static void onHeal(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent trigger = processor.get(TriggerComponent.@Key)
    if (trigger == null) return

    HealMultipleComponent heal = processor.get(HealMultipleComponent.@Key)
    if (heal == null) return

    double amount = trigger.getValue(attribute.id, 'total', 0.0d)
    heal.addHeal(attribute.id, amount)
}
