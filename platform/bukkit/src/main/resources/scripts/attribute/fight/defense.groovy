package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import groovy.transform.CompileStatic

@CompileStatic
static void onDefense(Attribute attribute, BukkitProcessor processor) {
    TargetComponent defender = processor.get(TargetComponent.@Key)
    if (defender == null) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    if (damage.getDamage('physical-attack') == null) return

    double value = defender.getValue(attribute.id, 'total')
    damage.takeDamage('physical-attack', value)
}
