# 冷却操作

`CooldownAPI` 操作某个实体会话上的具名冷却。每个冷却由一个字符串 `id` 标识，计时基于单调时钟，不受服务器校时影响。同一实体的不同功能应使用不同的 `id`。

::: info 前置知识
冷却挂在实体的 `AssiyaSession` 上。先用 `AssiyaAPI.getSessionOrCreate(entity)` 取得会话，再调用 `CooldownAPI`。非玩家实体同样拥有会话。
:::

## 方法一览

| 方法 | 说明 |
| --- | --- |
| `getRemainingMillis(session, id)` | 返回剩余冷却的毫秒数 |
| `getRemaining(session, id)` | 返回剩余冷却的 `Duration`，不存在时为零 |
| `set(session, id, durationMillis)` / `set(session, id, duration)` | 将冷却设为指定时长，覆盖已有计时 |
| `edit(session, id, transform)` | 以当前剩余时长为输入，经 `transform` 变换后重设，返回新的剩余时长 |
| `add(session, id, durationMillis)` / `add(session, id, duration)` | 在剩余时长上累加 |
| `sub(session, id, durationMillis)` / `sub(session, id, duration)` | 在剩余时长上扣减 |
| `isExpired(session, id)` | 不存在或已到期时返回 `true` |
| `tryAcquire(session, id, durationMillis)` / `tryAcquire(session, id, duration)` | 未在冷却时占用并设置冷却，返回 `true`；仍在冷却时返回 `false` |
| `remove(session, id)` | 移除该冷却条目 |
| `clear(session)` | 清空会话内的全部冷却 |

## 行为说明

- 时长为 `0`（或负数）等价于没有冷却：`set` 传零会直接移除条目，`sub` 扣到零及以下同样会移除。
- 读取剩余时长时若已到期，条目会被自动清除。
- `add` 与 `sub` 都基于 `edit` 实现，结果下限为 `0`。
- `tryAcquire` 是"未冷却才执行"的常用入口：可用时写入冷却并返回 `true`，已在冷却时不改动返回 `false`。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.CooldownAPI
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds

fun useSkill(player: Player): Boolean {
    val session = AssiyaAPI.getSessionOrCreate(player)

    // 未在冷却时占用 5 秒冷却
    if (!CooldownAPI.tryAcquire(session, "example:skill", 5.seconds)) {
        return false
    }

    // ... 执行技能逻辑
    return true
}

fun remainingSeconds(player: Player): Long {
    val session = AssiyaAPI.getSessionOrCreate(player)
    return CooldownAPI.getRemaining(session, "example:skill").inWholeSeconds
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI;
import github.mkbaka.assiya.platform.bukkit.api.CooldownAPI;
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession;
import org.bukkit.entity.Player;

public boolean useSkill(Player player) {
    AssiyaSession session = AssiyaAPI.getSessionOrCreate(player);

    // 未在冷却时占用 5000 毫秒冷却
    if (!CooldownAPI.tryAcquire(session, "example:skill", 5000L)) {
        return false;
    }

    // ... 执行技能逻辑
    return true;
}

public long remainingMillis(Player player) {
    AssiyaSession session = AssiyaAPI.getSessionOrCreate(player);
    return CooldownAPI.getRemainingMillis(session, "example:skill");
}
```
:::
