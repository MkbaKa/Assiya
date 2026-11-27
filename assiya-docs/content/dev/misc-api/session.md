# 实体会话

每个实体在 Assiya 中对应一个 `AssiyaSession`，保存该实体的属性、冷却、计数器、战斗状态、伤害栈和自定义数据。所有按实体隔离的状态都从这里取。

## 获取会话

| 方法 | 说明 |
| --- | --- |
| `AssiyaAPI.getSession(entity)` / `getSession(uuid)` | 查询已有会话，不存在返回 `null` |
| `AssiyaAPI.getSessionOrCreate(entity, withAttribute = true)` | 取得会话，不存在时创建 |

## AssiyaSession 成员

| 成员 | 说明 |
| --- | --- |
| `owner` | 当前绑定实体的弱引用，玩家退出或实体卸载后可能为 `null`；重新登录时绑定新的实体实例 |
| `ownerUUID` | 所属实体的 UUID |
| `combatState` | 战斗状态，仅玩家实体非空，详见[战斗状态操作](./combat-state.md) |
| `attribute` | 属性会话 `AttributeSession`，未初始化时访问会抛错，先用 `hasAttribute` 判断 |
| `hasAttribute` | 属性是否已初始化 |
| `cooldown` | 冷却存储，通常通过 `CooldownAPI` 操作 |
| `counter` | 计数器存储，通常通过 `CounterAPI` 操作 |
| `damageMetaStack` / `damageProcessorStack` | 当前伤害上下文栈，见下节 |
| `metadata` | 任意自定义数据表；`getMetadata<T>(key)` 按类型读取 |
| `refresh()` | 重新应用默认属性 |
| `close()` | 关闭会话，清理冷却、计数器、战斗状态、任务与属性会话 |

## AttributeSession

`AttributeSession` 是属性会话本身，通过 `session.attribute` 访问。

| 成员 | 说明 |
| --- | --- |
| `snapshot` | 当前属性快照 `AttributeEngineSnapshot`，只读 |
| `refreshDefaultAttributes()` | 按配置重算默认属性源 |
| `submit(vararg actions)` | 向属性引擎提交调度操作 |
| `isClosed` / `close()` | 是否已关闭 / 关闭 |

默认属性源 id 为 `assiya:default-attributes`，取值来自配置中的玩家与实体默认属性，优先级取 `default-attributes-priority`。

## 伤害上下文栈

`damageMetaStack` 与 `damageProcessorStack` 分别保存当前正在执行的 `DamageMeta` 与 `DamageProcessor`，提供 `current`、`push(...)`、`pop()`。`AssiyaAPI.isDamaging(entity)` 即通过判断 `damageMetaStack.current` 是否为空得知实体是否正在执行 Assiya 伤害。

## 生命周期

- 玩家进入时创建或取得会话、绑定实体并刷新装备属性。
- 玩家退出时解绑实体并取消尚未提交的装备刷新任务，实体任务的退出监听负责终止战斗计时任务并清理战斗状态；装备缓存保留到会话关闭或下次装备刷新。`settings.player-session-timeout`（默认 `600` 秒）大于 `0` 时保留会话至延时到期，否则立即移除。保留期间冷却、计数器和属性会话继续存在，临时属性来源仍可到期。
- 延时期间重新登录会重新绑定实体，使旧移除任务失效，并重新读取全部装备槽位。如果旧会话已经到期移除，则创建新会话。
- 非玩家实体在移出世界时移除会话。
- 已解绑会话到期时只释放自身的冷却、计数器和属性会话，不再取消重登后的实体任务。主动关闭仍绑定实体的会话时，也会清理其战斗状态和装备刷新任务。

是否启用玩家会话生命周期由 `settings.player-session-lifecycle`（默认 `true`）控制。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import org.bukkit.entity.Player

fun showSessionState(player: Player) {
    val session = AssiyaAPI.getSessionOrCreate(player)

    // 写入自定义数据
    session.metadata["example:last-use"] = System.currentTimeMillis()

    // 属性初始化后再读快照
    if (session.hasAttribute) {
        val snapshot = session.attribute.snapshot
        // ... 使用 snapshot
    }
}

fun readMetadata(player: Player): Long? =
    AssiyaAPI.getSession(player)?.getMetadata<Long>("example:last-use")
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI;
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession;
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot;
import org.bukkit.entity.Player;

public void showSessionState(Player player) {
    AssiyaSession session = AssiyaAPI.getSessionOrCreate(player);

    // 写入自定义数据
    session.getMetadata().put("example:last-use", System.currentTimeMillis());

    // 属性初始化后再读快照
    if (session.getHasAttribute()) {
        AttributeEngineSnapshot snapshot = session.getAttribute().getSnapshot();
        // ... 使用 snapshot
    }
}

public Long readMetadata(Player player) {
    AssiyaSession session = AssiyaAPI.getSession(player);
    return session == null ? null : session.getMetadata("example:last-use");
}
```
:::
