package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic

@CompileStatic
static void onDefense(Attribute attribute, BukkitProcessor processor) {
    if (processor.get(ProjectileComponent.@Key) == null) return

    TargetComponent defender = processor.get(TargetComponent.@Key)
    if (defender == null) return

    double chance = defender.getValue(attribute.id, 'total', 0.0d)
    if (!ScriptTools.chance(chance)) return

    processor.getComponent(CancellableComponent.@Key).setCancelled(true)

    CustomDataComponent customData = processor.get(CustomDataComponent.@Key)
    if (customData != null) {
        customData.put("is-remote-immune", true)
    }

    ScriptTools.sendCombatMessage(attribute, processor)
}
