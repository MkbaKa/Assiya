package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import groovy.transform.CompileStatic
import org.bukkit.entity.Player

@CompileStatic
static void onAttack(Attribute attribute, Processor processor) {
    TargetComponent target = processor.get(TargetComponent.@Key)
    if (target == null || !(target.entity instanceof Player)) return

    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    double value = attacker.getValue(attribute.id, 'total')
    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    damage.addDamage(attribute.id, value)
}
