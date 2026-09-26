package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, Processor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    TargetComponent target = processor.get(TargetComponent.@Key)
    if (target == null) return

    double chance = attacker.getValue(attribute.id, 'total', 0.0d)
    chance -= target.getValue('crit-resist', 'total', 0.0d)
    if (!ScriptTools.chance(chance)) return

    double dodge = target.getValue('crit-dodge', 'total', 0.0d)
    if (ScriptTools.chance(dodge)) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    double extraDamage = processor.formula('crit') { Map<String, Object> variables ->
        variables.put('damage', damage.totalDamage)
        variables.put('rate', attacker.getValue('crit-rate', 'total', 0.0d))
        variables.put('resist', target.getValue('crit-damage-resist', 'total', 0.0d))
    }
    damage.addDamage('crit', extraDamage)

    processor.get(CallbackComponent.@Key)?.callback {
        String extra = StringUtils.format(extraDamage, 2)
        String total = StringUtils.format(damage.totalDamage, 2)
        ScriptTools.getAttacker(processor)?.sendMessage("暴击! 对 ${target.entity.name} 额外造成 ${extra} 点暴击伤害, 最终伤害 ${total}.")
        target.entity.sendMessage("暴击! ${attacker.entity.name} 对你额外造成 ${extra} 点暴击伤害, 最终伤害 ${total}.")
    }
}
