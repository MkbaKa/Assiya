# 定时触发

timer 类型会以一个固定的间隔，**在后台对在线玩家执行属性的 onTimer 函数**。修改实体时，通过 `processor.launch` 切换到玩家所属的线程：Paper 使用主线程，Folia 使用玩家当前所在区域的线程。

适合生命恢复之类需要周期执行的属性。

## 触发间隔

通过在属性配置中指定 `period` 来修改该属性的定时执行间隔：

```yaml
health-regain:
  names: ['生命恢复']
  reader: default
  handler: 'attribute/health-regain'
  priority: 100
  // [!code focus]
  period: 20
  triggers: timer
  options:
    regain-in-combat: false
```

或者放在 `timer` 内：

```yaml
health-regain:
  names: ['生命恢复']
  reader: default
  handler: 'attribute/health-regain'
  priority: 100
  // [!code focus:3]
  triggers:
    timer:
      period: 20
  options:
    regain-in-combat: false
```

## 编写定时脚本

下面以生命恢复为例，脚本放在 `scripts/attribute/health-regain.groovy`。每次触发时读取生命恢复的值，再增加玩家的生命值，最多恢复到生命上限。

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
    if (trigger == null || !(trigger.entity instanceof Player)) return

    boolean regainInCombat = attribute.get(OptionsComponent.@Key)
            ?.getBoolean('regain-in-combat', false) ?: false

    Player player = (Player) trigger.entity
    processor.get(CallbackComponent.@Key)?.callback {
        double amount = trigger.getValue(attribute.id, 'total', 0.0d)
        if (amount <= 0.0d) return

        processor.<Player> launch(player) { Player entity ->
            if (!entity.isValid() || entity.isDead()) return
            if (!regainInCombat && CombatStateAPI.inCombat(entity)) return

            AssiyaAPI.heal(entity, amount)
        }
    }
}
```

玩家拥有 `生命恢复 5` 时，每次触发就会恢复 5 点生命。

::: tip 实体操作
在 `processor.launch` 中执行生命值、世界状态等实体操作。Folia 下，这个上下文只对应传入的实体；访问其他实体时，仍需确认当前线程是否拥有它。
:::
