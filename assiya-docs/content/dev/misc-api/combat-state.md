# 战斗状态操作

`CombatStateAPI` 维护玩家是否处于战斗状态，以及战斗中记录的目标。战斗状态会在指定时长后自动解除，再次进入会重新计时，因此可用于战斗脱战判断、禁止脱战回血等场景。

战斗状态只存在于玩家实体上：`AssiyaSession.combatState` 对非玩家实体为 `null`。

## 方法一览

| 方法 | 说明 |
| --- | --- |
| `join(player, target = null, durationTicks = 默认战斗时长)` | 进入战斗，可指定目标和持续时长 |
| `leave(player)` | 立即结束战斗状态 |
| `inCombat(player)` | 是否处于战斗状态 |

`join` 的 `durationTicks` 默认取配置 `settings.combat-duration`（单位为秒，默认 `10`），换算为 `200` tick。重复调用会重新计时，从而延长战斗状态。

## 行为说明

- 当战斗状态不存在，或目标发生改变时，会触发一次进入战斗的事件；重复以相同目标进入不会重复触发，只延长结束时间。
- 自动结束通过内部任务实现：计时到期后清除状态并触发离开战斗事件。
- `leave` 会同时取消计时任务并清除状态。

## 触发器

战斗状态的变化会派发对应事件：

| 事件 | 时机 |
| --- | --- |
| `CombatJoinEvent.Pre` | 进入战斗前，可取消 |
| `CombatJoinEvent.Post` | 进入战斗后 |
| `CombatLeaveEvent` | 离开战斗时 |

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import org.bukkit.entity.Player

fun tagCombat(player: Player, target: org.bukkit.entity.Entity) {
    // 进入战斗, 并在 8 秒后自动脱战
    CombatStateAPI.join(player, target, 8 * 20L)
}

fun stopCombat(player: Player) {
    CombatStateAPI.leave(player)
}

fun isInCombat(player: Player): Boolean = CombatStateAPI.inCombat(player)
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public void tagCombat(Player player, Entity target) {
    // 进入战斗, 并在 8 秒后自动脱战
    CombatStateAPI.join(player, target, 8 * 20L);
}

public void stopCombat(Player player) {
    CombatStateAPI.leave(player);
}

public boolean isInCombat(Player player) {
    return CombatStateAPI.inCombat(player);
}
```
:::
