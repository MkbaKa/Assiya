package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, Processor processor) {
    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)

    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    double value = attacker.getValue(attribute.id, 'total')

    TargetComponent target = processor.get(TargetComponent.@Key)
    damage.addDamage(attribute.id, value)

    processor.get(CallbackComponent.@Key)?.callback {
        /**
         * 当通过箭矢之类的抛射物触发伤害时 TriggerComponent 指向 箭矢实体本身
         * 但发送消息肯定不能发送给箭矢 所以要判断是否为远程伤害 然后取发射者
         * 对于脚本来说 这种判断比较麻烦 所以提供了一个工具函数
         * ScriptTools#getAttacker  简化了这一判断流程 其直接返回可能存在的 发射者/攻击者 而不是箭矢本身
         */
        ScriptTools.getAttacker(processor)?.sendMessage("你对 ${target.entity.getName()} 造成了 ${StringUtils.format(damage.getTotalDamage(), 2)} 点伤害.")
    }
}
