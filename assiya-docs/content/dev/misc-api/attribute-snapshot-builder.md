# 属性快照构建

`AttributeSnapshotBuilder` 在一份现有属性快照的基础上，构造一份新的、可修改的属性快照，而不影响原快照。常用于在不改动实体真实属性的前提下，推算"如果增加了这些属性会怎样"。

构造时可传入基础快照，不传则从空快照开始：

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeSnapshotBuilder

// 基于实体当前快照
val builder = AttributeSnapshotBuilder(AssiyaAPI.getSnapshot(entity))
```

## 方法一览

| 方法 | 说明 |
| --- | --- |
| `addAttribute(attributeId, captureId, value, sourceId = "snapshot:temporary", priority = null)` | 以单个捕获组的值增加属性 |
| `addAttribute(attributeId, status, sourceId = "snapshot:temporary", priority = null)` | 以完整 `Status` 增加属性 |
| `setAttribute(attributeId, status, sourceId = "snapshot:temporary", priority = null)` | 先移除该属性在所有来源中的贡献，再按给定来源设置 |
| `replaceSource(sourceId, attributes, priority = null)` | 整体替换某个来源的属性集合 |
| `removeSource(sourceId)` | 移除某个来源 |
| `build()` | 生成新的 `AttributeEngineSnapshot` |

`attributeId` 需为已注册的属性，否则该次操作会被忽略。`priority` 不传时沿用来源已有的优先级，新来源则为 `0`。默认来源 `snapshot:temporary` 用于不关心来源区分的临时改动。

`addAttribute`、`setAttribute`、`replaceSource`、`removeSource` 都返回构建器本身，可以链式调用，最后用 `build()` 得到快照。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeSnapshotBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import org.bukkit.entity.LivingEntity

fun damageWithBonus(attacker: LivingEntity, defender: LivingEntity): Double {
    // 基于攻击者当前快照, 额外增加 50 点伤害属性 (capture 名称取决于属性实现)
    val buffed = AttributeSnapshotBuilder(AssiyaAPI.getSnapshot(attacker))
        .addAttribute("example-damage", "total", 50.0)
        .build()

    // 用这份推算出的快照构造一次伤害处理, 不影响攻击者的真实属性
    val processor = DamageProcessor.ofSnapshot(
        attacker = attacker,
        defender = defender,
        attackerSnapshot = buffed
    )
    processor.process()
    return processor[DamageMultipleComponent.Key]?.totalDamage ?: 0.0
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI;
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeSnapshotBuilder;
import github.mkbaka.assiya.platform.bukkit.api.handler.WrappedProcessor;
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor;
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot;
import org.bukkit.entity.LivingEntity;

public double damageWithBonus(LivingEntity attacker, LivingEntity defender) {
    // 基于攻击者当前快照, 额外增加 50 点伤害属性 (链式调用)
    AttributeEngineSnapshot buffed = new AttributeSnapshotBuilder(AssiyaAPI.getSnapshot(attacker))
        .addAttribute("example-damage", "total", 50.0)
        .build();

    // 用推算出的快照构造一次伤害处理, 不影响攻击者的真实属性
    DamageProcessor processor = DamageProcessor.ofSnapshot(
        attacker, defender, null, buffed, AssiyaAPI.getSnapshot(defender), null
    );
    processor.process();
    return new WrappedProcessor(processor).getDamage();
}
```
:::
