# 定时触发

timer 类型会以一个固定的间隔，**异步对在线玩家执行属性的 onTimer 函数**。

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
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import groovy.transform.CompileStatic
import org.bukkit.entity.Player

@CompileStatic
static void onTimer(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent trigger = processor.get(TriggerComponent.@Key)
    if (trigger == null || !(trigger.entity instanceof Player)) return

    double amount = trigger.getValue(attribute.id, 'total', 0.0d)
    if (amount <= 0.0d) return

    boolean regainInCombat = attribute.get(OptionsComponent.@Key)
            ?.getBoolean('regain-in-combat', false) ?: false

    // 修改玩家生命值、判断战斗状态时, 必须切换到服务器主线程
    SchedulerUtils.callSync {
        Player player = (Player) trigger.entity
        if (!player.isValid() || player.isDead()) return
        if (!regainInCombat && CombatStateAPI.inCombat(player)) return

        player.health = Math.min(player.maxHealth, player.health + amount)
    }
}
```

玩家拥有 `生命恢复 5` 时，每次触发就会恢复 5 点生命。

::: warning 定时脚本默认在异步线程执行
修改生命值、判断战斗状态、操作世界等**非线程安全操作**，**必须且一定**要像示例一样放到 `SchedulerUtils.callSync` 中执行，**否则后果自负**。
:::