package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
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

    CustomDataComponent customData = processor.get(CustomDataComponent.@Key)
    if (customData != null) {
        customData.put("is-lightning", true)
        customData.put("lightning-damage", extraDamage)
    }

    processor.get(CallbackComponent.@Key)?.callback {
        ScriptTools.sendCombatMessage(
                attribute,
                processor,
                [damage: StringUtils.format(damage.totalDamage, 2), extra: StringUtils.format(extraDamage, 2)]
        )
    }
}
