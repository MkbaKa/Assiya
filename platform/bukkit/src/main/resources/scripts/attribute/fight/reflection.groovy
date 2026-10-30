package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic
import org.bukkit.entity.LivingEntity

@CompileStatic
static void onDefense(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    ProjectileComponent projectile = processor.get(ProjectileComponent.@Key)
    if (projectile != null) attacker = projectile.shooter
    if (attacker == null || !(attacker.entity instanceof LivingEntity)) return

    TargetComponent defender = processor.get(TargetComponent.@Key)
    if (defender == null) return

    double chance = defender.getValue(attribute.id, 'total', 0.0d)
    if (!ScriptTools.chance(chance)) return

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    double reflectedDamage = processor.formula('reflection') { Map<String, Object> variables ->
        variables.put('damage', damage.totalDamage)
        variables.put('rate', defender.getValue('reflection-rate', 'total', 0.0d))
    }

    AssiyaAPI.attackTo(defender.entity, (LivingEntity) attacker.entity, reflectedDamage)

    CustomDataComponent customData = processor.get(CustomDataComponent.@Key)
    if (customData != null) {
        customData.put("is-reflection", true)
        customData.put("reflection-damage", reflectedDamage)
    }

    ScriptTools.sendCombatMessage(
            attribute,
            processor,
            [reflection: StringUtils.format(reflectedDamage, 2)]
    )
}
