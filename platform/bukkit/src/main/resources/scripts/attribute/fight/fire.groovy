package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, BukkitProcessor processor) {
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

    CustomDataComponent customData = processor.get(CustomDataComponent.@Key)
    if (customData != null) {
        customData.put("is-fire", true)
        customData.put("fire-damage", extraDamage)
        customData.put("fire-ticks", ticks)
    }

    processor.get(CallbackComponent.@Key)?.callback {
        ScriptTools.sendCombatMessage(
                attribute,
                processor,
                [damage: StringUtils.format(damage.totalDamage, 2), extra: StringUtils.format(extraDamage, 2)]
        )
    }
}
