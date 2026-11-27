package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.TimerProcessor
import groovy.transform.CompileStatic
import org.bukkit.entity.Player

@CompileStatic
static void onTimer(Attribute attribute, TimerProcessor processor) {
    TriggerComponent trigger = processor.get(TriggerComponent.@Key)
    if (trigger == null) return
    if (!(trigger.entity instanceof Player)) return

    Player owner = (Player) trigger.entity

    boolean regainInCombat = attribute.get(OptionsComponent.@Key)?.getBoolean('regain-in-combat', false)

    processor.get(CallbackComponent.@Key)?.callback {
        double amount = trigger.getValue(attribute.id, 'total', 0.0d)
        if (amount <= 0.0d) return

        processor.<Player> launch(owner) { Player player ->
            if (!player.isValid() || player.isDead()) return
            if (!regainInCombat && CombatStateAPI.inCombat(player)) return

            AssiyaAPI.heal(player, amount)
        }
    }
}
