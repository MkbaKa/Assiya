# 属性与伤害操作

`AssiyaAPI` 是 Assiya 对外的总入口，覆盖属性查询、会话获取、装备刷新、属性文本解析与求值、属性的增加与移除，以及伤害和治疗。它是 `object`，方法均为静态可直接调用。

大部分属性相关方法返回 `CompletableFuture<AttributeEngineSnapshot>`：属性运算由内部的 `AttributeEngine` 调度执行，完成后以最新快照返回结果。

## 属性查询

| 方法 | 说明 |
| --- | --- |
| `getAttribute(idOrName)` | 按属性 `id` 或读取名查找已注册的 `Attribute`，未找到返回 `null` |

## 会话

| 方法 | 说明 |
| --- | --- |
| `getSession(entity)` / `getSession(uuid)` | 查询已有会话，不存在返回 `null` |
| `getSessionOrCreate(entity, withAttribute = true)` | 取得会话，不存在时创建；`withAttribute` 为 `true` 时初始化属性并刷新一次装备 |
| `getSnapshot(entity)` | 读取实体最近一次计算出的属性快照，无会话时返回空快照 |

`withAttribute` 只在首次初始化时生效，已有属性的会话不会重复初始化。

## 装备刷新

| 方法 | 说明 |
| --- | --- |
| `refreshEquipment(entity, vararg changedSlots)` | 重新读取实体的装备属性；不传槽位时刷新全部槽位 |

## 属性文本解析

| 方法 | 说明 |
| --- | --- |
| `parse(lines)` | 仅解析属性文本，返回可供 `evaluate` 使用的中间结果 |
| `parse(entity, lines)` | 解析并立即按实体判断条件，返回 `Map<String, AttributeValue<*>>` |
| `evaluate(entity, parsed, context)` | 对已解析结果执行条件判断与求值，返回 `Map<String, AttributeValue<*>>` |

## 增加属性

所有 `addAttribute` 都会把属性作为一个具名"属性源"（`sourceId`）加入会话。`priority` 为来源优先级，`lifeCycle` 控制来源生命周期（`Stable` 或 `Temporary(duration)`），`triggerEvent` 控制这次刷新是否派发属性更新事件。

| 重载 | 输入 |
| --- | --- |
| `addAttribute(session, sourceId, attributes: List<String>, priority, ...)` | 原始属性文本行 |
| `addAttribute(session, sourceId, itemStack, priority, ...)` | 从物品读取属性 |
| `addAttribute(session, sourceId, attributes: Map<String, AttributeValue<*>>, priority, ...)` | 已求值的属性值 |
| `addAttribute(session, sourceId, parsed, priority, ..., context)` | 已解析的属性来源，附带条件上下文 |

另有两条直接提交属性源的写法：

| 方法 | 说明 |
| --- | --- |
| `addAttribute(session, vararg patch)` | 提交一个或多个 `AttributeSourcePatch`，不触发更新事件 |
| `addAttribute(session, triggerEvent, vararg patch)` | 同上，可指定是否触发更新事件 |

以及两类批量操作：

| 方法 | 说明 |
| --- | --- |
| `addAttributeItems(session, sourceId, items, priority, ...)` | 从多个物品读取属性，合并为同一个属性源 |
| `addAttributeSnapshot(session, snapshot, lifeCycle = Stable, triggerEvent = false)` | 把一份快照中的所有属性源按原优先级加入，统一使用指定生命周期；`triggerEvent` 控制是否触发属性更新事件 |

## 移除属性

| 方法 | 说明 |
| --- | --- |
| `takeAttribute(entity, sourceId)` | 移除实体上的指定属性源，无会话时返回空快照 |
| `takeAttribute(session, sourceId)` | 移除会话上的指定属性源 |

## 伤害与治疗

| 方法 | 说明 |
| --- | --- |
| `attackTo(attacker, defender, damage)` | 对受击者造成一次固定伤害，经过 Assiya 包装，等价于 `DamageMeta.fixedDamage(...).doDamage()` |
| `isDamaging(attacker)` | 该实体是否正在执行 Assiya 的伤害 |
| `heal(entity, value, builder = null)` | 经过 Assiya 治疗流程治疗实体，可用 `builder` 追加处理器组件 |

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import org.bukkit.entity.Player

fun grantBonus(player: Player, sourceId: String, lines: List<String>) {
    val session = AssiyaAPI.getSessionOrCreate(player)

    // 以优先级 100 增加一组属性文本, 并触发一次属性更新事件
    AssiyaAPI.addAttribute(
        session = session,
        sourceId = sourceId,
        attributes = lines,
        priority = 100,
        triggerEvent = true
    ).thenAccept { snapshot ->
        // snapshot 为刷新后的最新属性快照
    }
}

fun removeBonus(player: Player, sourceId: String) {
    AssiyaAPI.takeAttribute(player, sourceId)
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI;
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession;
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle;
import org.bukkit.entity.Player;

import java.util.List;

public void grantBonus(Player player, String sourceId, List<String> lines) {
    AssiyaSession session = AssiyaAPI.getSessionOrCreate(player);

    // 以优先级 100 增加一组属性文本, 并触发一次属性更新事件
    AssiyaAPI.addAttribute(session, sourceId, lines, 100, AttributeSourceLifecycle.Stable, true)
        .thenAccept(snapshot -> {
            // snapshot 为刷新后的最新属性快照
        });
}

public void removeBonus(Player player, String sourceId) {
    AssiyaAPI.takeAttribute(player, sourceId);
}
```
:::
