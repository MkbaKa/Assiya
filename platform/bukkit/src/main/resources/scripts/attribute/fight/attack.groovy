package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, Processor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)

    double value = attacker.getValue(attribute.id, 'total')
    damage.addDamage(attribute.id, value)

    processor.get(CallbackComponent.@Key)?.callback {
        ScriptTools.sendCombatMessage(
                attribute,
                processor,
                [total: StringUtils.format(damage.totalDamage, 2), damage: StringUtils.format(value, 2)]
        )
    }
}
