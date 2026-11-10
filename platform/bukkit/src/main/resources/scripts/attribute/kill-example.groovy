package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByKillComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic

@CompileStatic
static void onKill(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent killer = processor.get(TriggerComponent.@Key)
    TriggerByKillComponent kill = processor.get(TriggerByKillComponent.@Key)
    if (killer == null || kill == null) return

    kill.reviveHealth = killer.getValue(attribute.id, 'total', 0.0d)
    processor.get(CancellableComponent.@Key).setCancelled(true)

    processor.get(CallbackComponent.@Key)?.callback(true) {
        ScriptTools.sendCombatMessage(attribute, processor, [health: kill.reviveHealth])
    }
}
