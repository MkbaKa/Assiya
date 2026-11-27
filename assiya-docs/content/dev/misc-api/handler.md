# 处理器与组件

Assiya 用 `Processor`（处理器）表示一次任务的执行过程，用 `Component`（组件）承载具体能力与数据。处理器本身不固定任何字段，所有信息都放在组件里，因此可以通过组合组件扩展行为，而不必改动 Assiya 源码。设计动机详见 [Processor](../advanced/processor.md)，组件清单另见[已实现的组件](../advanced/components.md)。

## BukkitProcessor

`BukkitProcessor` 是平台层的处理器接口，提供公式调用与默认组件：

| 成员 | 说明 |
| --- | --- |
| `formula(id, variables)` | 按公式名与变量计算结果 |
| `formula(id, builder)` | 同上，用 `Consumer` 构造变量表 |
| `process()` | 执行入口，各具体处理器实现 |
| `BukkitProcessor.defaults()` | 生成含基础组件的 `ProcessorComponentBuilder` |

`defaults()` 会放入 `CallbackComponent`、`CancellableAttributeComponent`、`CancellableComponent`、`CustomDataComponent`。

具体处理器的 `process()` 一般先向 `HandlerRegistry` 发布，再调用 `CallbackComponent` 的回调。

## WrappedProcessor

`WrappedProcessor` 包装一个 `BukkitProcessor`，提供便捷读取与修改：

| 成员 | 说明 |
| --- | --- |
| `attackerOrKiller` | 攻击者或击杀者 |
| `entity` | 目标实体（优先 `TargetComponent`，否则 `TriggerComponent`） |
| `isProjectile` / `isSkillDamage` | 是否抛射物伤害 / 技能伤害 |
| `getDamage()` / `setDamage(value)` / `updateDamage(value)` | 读取、设置、增减本次伤害 |
| `isCancelled` / `setCancelled(...)` | 读写取消状态（代理 `CancellableComponent`） |

## 内置处理器

| 处理器 | 工厂方法 | 用途 |
| --- | --- | --- |
| `DamageProcessor` | `of(attacker, defender, builder)`、`ofSnapshot(...)`、`ofProjectile(...)`、`ofLazyTarget(...)` | 一次伤害处理 |
| `HealProcessor` | `of(entity, value, builder)` | 一次治疗处理 |
| `KillProcessor` | `of(killer, victim, event)` | 一次击杀处理 |
| `TimerProcessor` | `of(entity, snapshot, subscribers)` | 定时触发 |
| `UpdateProcessor` | `of(session, context)` | 属性更新 |

`UpdateProcessor` 提供所属 `session` 和本轮 `context`，并直接实现 `AttributeContainer`。使用 `processor.getValue(...)` 读取本轮属性，使用 `processor.context` 修改本轮来源；这些操作不要求实体在线。`processor.session.owner` 返回当前绑定的实体，可能为 null。工厂仅在 owner 存在时添加 `TriggerComponent`，它与处理器共享同一属性容器。

`UpdateProcessor` 和 `TimerProcessor` 都提供 `launch(entity, action)`，将实体操作放到对应实体上下文执行。Java/Groovy 使用 `Consumer<E>`，Groovy 静态编译时可写成 `processor.<Player> launch(player) { Player entity -> ... }`；Kotlin 也可传入挂起代码块。后台逻辑应在调度前取得本轮属性数值，避免在异步执行的实体操作中继续修改本轮 `context`。

`AttributeUpdateEvent.Pre/Post` 与 `AttributeSourceRemoveEvent.Pre/Post` 均提供 `session`，需要实体时读取 `event.session.owner`。事件沿后台引擎执行，取得实体引用后仍需在它的所属线程执行实时实体读写。

`DamageProcessor` 还提供 `fork(builder)`，基于当前组件数据复制出一个新的处理器，仅复制实现了 `ForkableComponent` 的组件。

## 组件

组件实现 `ProcessorComponent`，以 `Key` 在处理器中存取。常用组件：

| 组件 | 能力 |
| --- | --- |
| `CallbackComponent` | 处理器结束后的回调，可设置仅在未取消时执行 |
| `CancellableComponent` | 整个处理器的取消状态 |
| `CancellableAttributeComponent` | 记录被跳过执行的属性 |
| `CustomDataComponent` | 在处理过程中跨属性传递数据 |
| `DamageComponent` / `DamageMultipleComponent` | 单个伤害条目 / 多条目伤害（总和为非负的 `totalDamage`） |
| `HealComponent` / `HealMultipleComponent` | 单个治疗条目 / 多条目治疗 |
| `TriggerComponent` | 触发器来源实体与其快照 |
| `TargetComponent` / `TargetDefaultComponent` / `TargetLazyComponent` | 目标实体（通用接口 / 默认 / 延迟指定） |
| `TriggerByEventComponent` | 由 `EntityDamageByEntityEvent` 触发的数据 |
| `TriggerByKillComponent` | 由 `EntityDeathEvent` 触发的数据 |
| `TriggerByMythicSkillComponent` | 由 MythicMobs 技能触发的数据 |
| `ProjectileComponent` | 抛射物相关信息 |
| `VanillaAttributesComponent` | 原版属性操作入口 |

`TriggerComponent`、`TargetDefaultComponent`、`TargetLazyComponent` 都实现 `EntityProcessorComponent`（提供 `entity`、`snapshot`、`session`）与 `AttributeContainer`，可在处理过程中读取或临时修改属性。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import org.bukkit.entity.LivingEntity

fun applyDamage(attacker: LivingEntity, defender: LivingEntity) {
    val processor = DamageProcessor.of(attacker, defender) { pcb ->
        // 追加自定义数据组件, 供本次处理中的属性共享
        pcb += CustomDataComponent()
    }

    processor.process()

    // 处理结束后读取累计伤害
    val total = processor[DamageMultipleComponent.Key]?.totalDamage ?: 0.0
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.handler.WrappedProcessor;
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent;
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor;
import org.bukkit.entity.LivingEntity;

public void applyDamage(LivingEntity attacker, LivingEntity defender) {
    DamageProcessor processor = DamageProcessor.of(attacker, defender, pcb -> {
        // 追加自定义数据组件, 供本次处理中的属性共享
        pcb.add(new CustomDataComponent());
    });

    processor.process();

    // 处理结束后读取累计伤害
    double total = new WrappedProcessor(processor).getDamage();
}
```
:::
