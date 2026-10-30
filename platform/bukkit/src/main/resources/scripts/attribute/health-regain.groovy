package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import groovy.transform.CompileStatic
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

@CompileStatic
static void onTimer(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent trigger = processor.get(TriggerComponent.@Key)
    if (trigger == null) return
    if (!(trigger.entity instanceof LivingEntity)) return

    boolean regainInCombat = attribute.get(OptionsComponent.@Key)?.getBoolean('regain-in-combat', false)

    processor.get(CallbackComponent.@Key)?.callback {
        double amount = trigger.getValue(attribute.id, 'total', 0.0d)
        if (amount <= 0.0d) return

        SchedulerUtils.callSync {
            Player entity = (Player) trigger.entity
            if (!entity.isValid() || entity.isDead()) return
            if (!regainInCombat && CombatStateAPI.inCombat(entity)) return

            entity.health = Math.min(entity.maxHealth, entity.health + amount)
        }
    }
}
