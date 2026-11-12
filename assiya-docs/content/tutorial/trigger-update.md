# 属性更新时触发

这个触发器会在调用 `AssiyaAPI#addAttribute` 并指定 `triggerEvent` 参数为 true 的情况下触发。

请避免在这一触发器中做上述行为，否则会导致递归触发。

如果你想实现加成之类的属性，在 update 时增加属性，那么我建议你使用 `AttributeUpdateContext`。

下面以“每点力量增加 2 点物理伤害”为例：

```yaml
strength:
  names: ['力量']
  reader: default
  handler: 'attribute/strength'
  triggers:
    update:
      always-run: true
```

```groovy :line-numbers
package scripts.attribute

import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import groovy.transform.CompileStatic

@CompileStatic
class Strength {

    private static final String SOURCE_ID = 'derived:strength'

    static void onUpdate(Attribute attribute, UpdateProcessor processor) {
        TriggerComponent trigger = processor.get(TriggerComponent.@Key)
        if (trigger == null) return

        AttributeUpdateContext context = processor.context
        double strength = trigger.getValue(attribute.id, 'total', 0.0d)

        // 不再满足加成条件时, 移除之前生成的属性源
        if (strength <= 0.0d) {
            context.remove(SOURCE_ID)
            return
        }

        Map<String, AttributeValue<?>> attributes = AssiyaAPI.parse(
                trigger.entity,
                Arrays.asList("物理伤害: ${strength * 2.0d}")
        )

        if (context.get(SOURCE_ID) == null) {
            // 来源不存在时, 提交一份新的属性源
            context.submit(new AttributeSourcePatch(
                    SOURCE_ID,
                    0,
                    AttributeSourceLifecycle.Stable,
                    attributes
            ))
        } else {
            // 来源已存在时, 替换它的属性内容并保留原有优先级与生命周期
            context.modify(SOURCE_ID, attributes, null, false, null)
        }
    }
}
```

## 加成类属性的清理

既然有加成逻辑，就要做没有属性之后的清理逻辑。但既然没有属性了，属性该如何触发？

很简单，像力量属性一样，将 `update` 触发器的 `always-run` 选项打开：

```yaml
strength:
  names: ['力量']
  reader: default
  handler: 'attribute/strength'
  // [!code focus:3]
  triggers:
    update:
      always-run: true
```

打开该选项后，即使玩家身上没有力量属性，也能触发这段脚本逻辑，就能借此实现清理逻辑了。

记得判断属性值哦。