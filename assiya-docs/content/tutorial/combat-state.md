# 战斗状态

战斗状态是由 Assiya 自发维护的，其实现位于 `scripts/feature/CombatStateListener.groovy` 脚本中。

*默认情况下，会在玩家造成/受到攻击时进入持续 `10` 秒的战斗状态。*

## 实际效果

战斗状态没有任何实际功能，它只是用来给脚本、技能条件判断用的标记。

比如，默认属性的 `生命恢复` 就用到了这一点：

```yaml :line-numbers
health-regain:
  names: ['生命恢复']
  reader: default
  handler: 'attribute/health-regain'
  priority: 100
  # 触发间隔 (20tick = 1s)
  period: 20
  triggers: timer
  // [!code focus:4]
  options:
    # 战斗状态中能否触发恢复
    # 为 false 则只在脱战状态恢复
    regain-in-combat: false
```

脚本中的判断逻辑为：

```groovy :line-numbers
package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.TimerProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
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

        processor.<Player> launch(owner) { Player entity ->
            if (!entity.isValid() || entity.isDead()) return
            // [!code focus:3]
            // 若 options 内的 regain-in-combat 为 false
            // 且该玩家处于战斗状态中 则不回复生命
            if (!regainInCombat && CombatStateAPI.inCombat(entity)) return

            AssiyaAPI.heal(entity, amount)
        }
    }
}

```
