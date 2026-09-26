package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic

@CompileStatic
static void onDefense(Attribute attribute, Processor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    TargetComponent defender = processor.get(TargetComponent.@Key)
    if (defender == null) return

    // TODO: 配置模块与冷却工具接入后支持玩家闪避冷却, 默认冷却为 0.
    double chance = defender.getValue(attribute.id, 'total', 0.0d)
    chance -= attacker.getValue('hit', 'total', 0.0d)
    if (!ScriptTools.chance(chance)) return

    processor.getComponent(CancellableComponent.@Key).setCancelled(true)

    ScriptTools.getAttacker(processor)?.sendMessage("${defender.entity.name} 闪避了你的攻击.")
    defender.entity.sendMessage("你避开了 ${attacker.entity.name} 的攻击.")
}
