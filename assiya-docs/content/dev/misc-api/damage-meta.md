# 伤害元数据

`DamageMeta` 唤起一次经过 Assiya 处理的伤害。它携带一个 `DamageProcessor`（本次伤害的上下文与组件），并在执行时按阶段派发伤害事件、运行属性效果，最后对受击者造成伤害。

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import org.bukkit.entity.LivingEntity

// process 为 true 时执行属性效果
val meta = DamageMeta(
    DamageProcessor.of(attacker, defender) { pcb -> /* 追加组件 */ }
)
meta.doDamage()
```

`processor` 为本次伤害的 `DamageProcessor`；`process` 表示是否执行属性效果（为 `false` 时只做结算，不跑属性逻辑）。

## 可调字段

| 字段 | 默认 | 作用 |
| --- | --- | --- |
| `ignoreImmunity` | `false` | 结算前清零受击者的无敌时间，忽略受击间隔 |
| `preventKnockback` | `false` | 结算时临时将击退抗性设为满值，避免击退 |
| `noDamageTicks` | `null` | 结算后把受击者的无敌时间设为该值 |
| `preDamage` | `null` | 结算前的谓词，返回 `true` 则取消本次伤害 |
| `postDamage` | `null` | 结算后的回调 |

## 执行阶段

`doDamage()` 依次经过以下阶段，对应的 `DamageMetaEvent` 可在各阶段介入：

1. `Start`：开始，可取消。
2. `process` 为 `true` 时：`PreProcess`（可取消）→ 执行属性效果 → `PostProcess`。
3. `PreDamage`：结算前，可取消。
4. 检查 `CancellableComponent` 的取消状态与 `preDamage`。
5. 对受击者结算伤害（处理 `ignoreImmunity`、`preventKnockback`、`noDamageTicks`）。
6. `PostDamage` 与 `postDamage` 回调。
7. `End`：结束。

## 工厂方法

| 方法 | 说明 |
| --- | --- |
| `fixedDamage(attacker, defender, damage, builder = null)` | 构造一次固定伤害，不触发属性逻辑，等价于 `LivingEntity.damage` 但经过 Assiya 包装 |
| `lazyTarget(attacker, builder = null)` | 构造一次目标延迟指定的伤害，需在 `doDamage()` 前通过 `TargetLazyComponent.set(...)` 设定目标 |

`fixedDamage` 会把伤害写入 `DamageMultipleComponent` 的 `vanilla` 条目。`builder` 用于在默认组件基础上追加自己的组件。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import org.bukkit.entity.LivingEntity
import java.util.function.Consumer

fun hit(attacker: LivingEntity, defender: LivingEntity, amount: Double) {
    val meta = DamageMeta.fixedDamage(attacker, defender, amount)
    meta.ignoreImmunity = true
    meta.preventKnockback = true

    // 结算之后执行
    meta.postDamage = Consumer {
        // ... 命中后的反馈逻辑
    }

    meta.doDamage()
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta;
import org.bukkit.entity.LivingEntity;

public void hit(LivingEntity attacker, LivingEntity defender, double amount) {
    DamageMeta meta = DamageMeta.fixedDamage(attacker, defender, amount);
    meta.setIgnoreImmunity(true);
    meta.setPreventKnockback(true);

    // 结算之后执行
    meta.setPostDamage(m -> {
        // ... 命中后的反馈逻辑
    });

    meta.doDamage();
}
```
:::
