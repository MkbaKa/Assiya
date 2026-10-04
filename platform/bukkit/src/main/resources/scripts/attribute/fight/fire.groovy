package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, Processor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    TargetComponent target = processor.get(TargetComponent.@Key)
    if (target == null) return

    double chance = attacker.getValue(attribute.id, 'total', 0.0d)
    if (!ScriptTools.chance(chance)) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    double extraDamage = processor.formula('fire') { Map<String, Object> variables ->
        variables.put('damage', attacker.getValue('fire-damage', 'total', 0.0d))
    }
    damage.addDamage('fire', extraDamage)
    double duration = 3.0d + attacker.getValue('fire-time', 'total', 0.0d)
    int ticks = (int) Math.max(0.0d, Math.ceil(duration * 20.0d))
    target.entity.setFireTicks(ticks)

    processor.get(CallbackComponent.@Key)?.callback {
        ScriptTools.sendCombatMessage(attribute, processor, [damage: damage.totalDamage, extra: extraDamage])
    }
}
