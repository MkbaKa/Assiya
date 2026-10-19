package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic

@CompileStatic
static void onDefense(Attribute attribute, Processor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    TargetComponent defender = processor.get(TargetComponent.@Key)
    if (defender == null) return

    CustomDataComponent customData = processor.get(CustomDataComponent.@Key)

    double sunder = attacker.getValue('sunder-armor', 'total', 0.0d)
    if (ScriptTools.chance(sunder)) {
        if (customData != null) {
            customData.put("is-sunder-armor", true)
        }
        return
    }

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    double reduction = processor.formula('armor') { Map<String, Object> variables ->
        variables.put('damage', damage.totalDamage)
        variables.put('armor', defender.getValue(attribute.id, 'total', 0.0d))
        variables.put('penetration', attacker.getValue('see-through', 'total', 0.0d))
    }
    damage.takeDamage('armor', reduction)

    if (customData != null) {
        customData.put("armor-reduction", reduction)
    }
}
