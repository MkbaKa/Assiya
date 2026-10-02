package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic
import org.bukkit.entity.LivingEntity

@CompileStatic
class Vampire {

    static void onAttack(Attribute attribute, Processor processor) {
        TriggerComponent attacker = processor.get(TriggerComponent.@Key)
        if (attacker == null) return

        TargetComponent target = processor.get(TargetComponent.@Key)
        if (target == null) return
        if (!(attacker.entity instanceof LivingEntity)) return

        double chance = attacker.getValue(attribute.id, 'total', 0.0d)
        chance -= target.getValue('vampire-resist', 'total', 0.0d)
        if (!ScriptTools.chance(chance)) return

        double dodge = target.getValue('vampire-dodge', 'total', 0.0d)
        if (ScriptTools.chance(dodge)) return

        DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
        double heal = processor.formula('vampire') { Map<String, Object> variables ->
            variables.put('damage', damage.totalDamage)
            variables.put('rate', attacker.getValue('vampire-rate', 'total', 0.0d))
        }

        boolean extraDamage = attribute.get(OptionsComponent.@Key)?.getBoolean("extra-damage", false)
        if (extraDamage) {
            damage.addDamage('vampire', heal)
        }

        if (!attacker.entity.isDead() && heal > 0.0d) {
            LivingEntity entity = (LivingEntity) attacker.entity
            double health = entity.health
            double maxHealth = entity.maxHealth
            entity.health = Math.min(maxHealth, health + heal)
        }

        processor.get(CallbackComponent.@Key)?.callback {
            ScriptTools.sendCombatMessage(attribute, processor, [damage: damage.totalDamage, heal: heal])
        }
    }
}
