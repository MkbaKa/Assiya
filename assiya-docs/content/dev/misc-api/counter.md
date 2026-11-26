# 计数器操作

`CounterAPI` 操作某个实体会话上的具名计数器。计数器可以保存一个数值、一段文本，以及若干带过期时间的子值，适合实现连击数、层数、累计次数等随时增减的状态。

::: info 前置知识
计数器挂在实体的 `AssiyaSession` 上。先用 `AssiyaAPI.getSessionOrCreate(entity)` 取得会话。
:::

## 获取与移除

| 方法 | 说明 |
| --- | --- |
| `getOrCreate(session, key, type)` | 取得计数器，不存在时按指定重置类型创建 |
| `get(session, key)` | 取得计数器，不存在时返回 `null` |
| `contains(session, key)` | 判断是否存在 |
| `remove(session, key)` | 移除指定计数器 |
| `clear(session)` | 清空会话内的全部计数器 |

`type` 为 `CounterResetType`：

| 类型 | 行为 |
| --- | --- |
| `DEFAULT` | 默认，不随实体死亡重置 |
| `DEATH` | 实体死亡时自动重置（由内部监听 `EntityDeathEvent` 触发） |

重置类型在创建时确定，之后不会改变。

## 数值

| 方法 | 说明 |
| --- | --- |
| `getValue(default = 0.0, child = false)` | 读取值；`child` 为 `true` 时累加所有子值 |
| `setValue(next)` | 直接设置值 |
| `editValue(default, transform)` | 以当前值（为空时用 `default`）经 `transform` 变换后设置 |
| `add(amount, default = 0.0)` / `sub(amount, default = 0.0)` | 增加或减少 |
| `resetValue()` | 清空值，恢复为未设置 |
| `isDefaultValue()` | 值是否处于未设置状态 |

## 文本

| 方法 | 说明 |
| --- | --- |
| `getContent(default)` | 读取文本，为空时返回 `default` |
| `setContent(next)` | 设置文本 |
| `resetContent()` | 清空文本 |
| `isDefaultContent()` | 文本是否处于未设置状态 |

## 子值

子值以名称区分，可携带各自的过期时长，适合表示多来源叠加的临时层数。

| 方法 | 说明 |
| --- | --- |
| `addChildValue(name, value, duration = null)` | 新增或覆盖同名子值，`duration` 到期后自动移除 |
| `getChildOrNull(name)` | 读取指定子值，不存在时返回 `null` |
| `getChildValue()` | 所有子值之和 |
| `isChildValueEmpty()` | 是否没有子值 |
| `removeChild(name)` | 移除指定子值 |
| `resetChildValue()` | 清空所有子值 |
| `takeChildValue(amount)` | 按加入顺序从正值子值中扣除，返回未能扣除的余量 |

## 自动重置

| 方法 | 说明 |
| --- | --- |
| `setNextAutoReset(duration, repeat = true)` | 在 `duration` 后重置；`repeat` 为 `true` 时循环重置 |
| `cancelAutoReset()` | 取消自动重置 |
| `reset()` | 清空数值、文本与全部子值，但不改变重置类型与自动重置设置 |

到期清理在每次访问计数器时进行：过期的子值被移除，到达重置时间的数值与文本被清空。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.CounterAPI
import github.mkbaka.assiya.platform.bukkit.api.counter.CounterResetType
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds

fun addCombo(player: Player, stacks: Double) {
    val session = AssiyaAPI.getSessionOrCreate(player)
    // 死亡时清零，层数在 5 秒无新增后过期
    val counter = CounterAPI.getOrCreate(session, "example:combo", CounterResetType.DEATH)
    counter.addChildValue("hit", stacks, 5.seconds)
    counter.add(stacks)
}

fun readCombo(player: Player): Double {
    val session = AssiyaAPI.getSessionOrCreate(player)
    return CounterAPI.get(session, "example:combo")?.getValue() ?: 0.0
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI;
import github.mkbaka.assiya.platform.bukkit.api.CounterAPI;
import github.mkbaka.assiya.platform.bukkit.api.counter.Counter;
import github.mkbaka.assiya.platform.bukkit.api.counter.CounterResetType;
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession;
import org.bukkit.entity.Player;

public void addCombo(Player player, double stacks) {
    AssiyaSession session = AssiyaAPI.getSessionOrCreate(player);
    // 死亡时清零
    Counter counter = CounterAPI.getOrCreate(session, "example:combo", CounterResetType.DEATH);
    counter.addChildValue("hit", stacks);
    counter.add(stacks);
}

public double readCombo(Player player) {
    AssiyaSession session = AssiyaAPI.getSessionOrCreate(player);
    Counter counter = CounterAPI.get(session, "example:combo");
    return counter == null ? 0.0 : counter.getValue();
}
```
:::
