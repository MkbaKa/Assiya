package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByEventComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic

@CompileStatic
static void onKill(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent killer = processor.get(TriggerComponent.@Key)
    TriggerByEventComponent event = processor.get(TriggerByEventComponent.@Key)
    if (killer == null || event == null) return

    processor.get(CallbackComponent.@Key)?.callback {
        ScriptTools.sendCombatMessage(attribute, processor)
    }
}
