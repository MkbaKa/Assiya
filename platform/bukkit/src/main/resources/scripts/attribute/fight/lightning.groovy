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
    if (!ScriptTools.chance(chance)) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    double extraDamage = processor.formula('lightning') { Map<String, Object> variables ->
        variables.put('damage', attacker.getValue('lightning-damage', 'total', 0.0d))
    }
    target.entity.world.strikeLightningEffect(target.entity.location)
    damage.addDamage('lightning', extraDamage)

    processor.get(CallbackComponent.@Key)?.callback {
        String extra = StringUtils.format(extraDamage, 2)
        ScriptTools.getAttacker(processor)?.sendMessage("雷击! 额外造成 ${extra} 点雷击伤害!")
        target.entity.sendMessage("雷击! 对方对你额外造成 ${extra} 点雷击伤害!")
    }
}
