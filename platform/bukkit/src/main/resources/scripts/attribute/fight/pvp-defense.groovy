package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import groovy.transform.CompileStatic
import org.bukkit.entity.Player

@CompileStatic
static void onDefense(Attribute attribute, Processor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null || !(attacker.entity instanceof Player)) return

    TargetComponent defender = processor.get(TargetComponent.@Key)
    if (defender == null) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    if (damage.getDamage('pvp-attack') == null) return

    double value = defender.getValue(attribute.id, 'total')
    damage.takeDamage('pvp-attack', value)
}
