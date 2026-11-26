# 触发器

触发器定义属性在什么过程中执行，以及执行时调用脚本里的哪个函数。可以通过 `TriggerRegistry` 注册新的触发器，再由对应的处理器发布这次过程。


## 注册触发器

这里以默认的 `heal` 触发器为例，注册脚本位于 `scripts/handler/trigger/heal.groovy`。

```groovy :line-numbers
package scripts.handler.trigger

import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.TriggerRegistry
import groovy.transform.CompileStatic

@CompileStatic
class Heal {

    // 方便注销
    private TriggerRegistry.TriggerType registration

    void onEnable() {
        /**
         * TriggerBase 的三个参数分别是 触发器名称、触发时的函数、索引名
         * 也就是：
         *      heal    ->  触发器名称
         *      onHeal  ->  函数名
         *      heal    ->  索引名
         *      
         * 索引名用于像 attack 与 defense 这种同时触发、且共享优先级的情况
         * 普通触发器不用管
         */
        TriggerRegistry.INSTANCE.register(new Trigger.Base('heal', 'onHeal', 'heal')) {
            BukkitProcessor processor, String id ->
                return processor.get(TriggerComponent.@Key)?.hasAttribute(id) ?: false
        }
        registration = TriggerRegistry.INSTANCE.get('heal')
    }

    void onDisable() {
        if (registration == null) return
        TriggerRegistry.INSTANCE.unregister('heal', registration)
        registration = null
    }

}
```

## 让属性使用触发器

默认的“额外恢复值”属性使用 `heal` 触发器：

```yaml
health-regain-addition:
  names: ['额外恢复值']
  reader: default
  handler: 'attribute/health-regain-addition'
  priority: 100
  triggers: heal
```

对应的 `scripts/attribute/health-regain-addition.groovy` 定义 `onHeal`，参数依次为当前属性和本次过程数据：

```groovy :line-numbers
package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.HealMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import groovy.transform.CompileStatic

@CompileStatic
static void onHeal(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent trigger = processor.get(TriggerComponent.@Key)
    if (trigger == null) return

    HealMultipleComponent heal = processor.get(HealMultipleComponent.@Key)
    if (heal == null) return

    double amount = trigger.getValue(attribute.id, 'total', 0.0d)
    heal.addHeal(attribute.id, amount)
}
```

## 发布触发

注册触发器后，还需要在对应的业务过程中发布它。`heal` 已由 `HealProcessor` 实现，其 `process()` 中的发布入口是：

```kotlin
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry

HandlerRegistry.publish("heal", this)
```

这里的 `"heal"` 对应 `indexId`。发布时会遍历该索引下的属性 handler，按 `priority` 升序执行。