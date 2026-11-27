# 实体任务操作

`EntityTaskAPI` 在实体上按 tick 调度延迟或周期任务。任务以 `taskId` 注册行为，运行时以实体和 `taskId` 创建实例，并通过 `key` 区分同一实体的不同任务实例。

## 注册任务

先用 `registerTask` 注册一个 `taskId` 对应的行为，之后才能运行：

| 参数 | 说明 |
| --- | --- |
| `taskId` | 任务标识，运行时按它查找已注册行为 |
| `task` | 任务执行时调用，参数为实体与运行时 `params` |
| `onTerminate` | 任务被终止时调用，可不传 |

## 运行任务

| 方法 | 说明 |
| --- | --- |
| `runTaskKt(owner, taskId, builder)` | Kotlin 用法，`builder` 为 `EntityTaskBuilder.() -> Unit` |
| `runTask(owner, taskId, builder)` | Java 用法，`builder` 为 `Consumer<EntityTaskBuilder>` |

返回 `EntityTask`，可调用其 `cancel()` 取消。`EntityTaskBuilder` 的配置项：

| 字段 | 默认 | 说明 |
| --- | --- | --- |
| `key` | `null` | 任务实例标识；同一实体存在同 `key` 的任务时，旧任务被替换（不触发 `onTerminate`） |
| `delay` | `0` | 首次执行前的延迟，单位 tick |
| `period` | `0` | 两次执行之间的间隔，单位 tick |
| `count` | `1` | 执行次数；为 `null` 或非正数时不执行 |
| `async` | `false` | 为 `false` 时在实体归属线程执行，为 `true` 时在后台执行 |
| `params` | 空 | 传给 `task` 与 `onTerminate` 的运行时数据 |

## 取消与终止

| 方法 | 说明 |
| --- | --- |
| `cancelTask(owner, key)` / `cancelTask(ownerUUID, key)` | 取消指定实体上指定 `key` 的任务 |
| `EntityTask.cancel()` | 取消单个任务实例 |

自然执行完毕和同 `key` 替换不会触发 `onTerminate`。主动取消、执行异常、玩家退出和普通实体移出世界时会终止任务。Paper 在触发终止的线程调用清理回调；Folia 通过实体协程执行清理，跨线程取消时，`cancel()` 返回后清理可能仍在等待执行。已经提交的旧任务清理不会因同 `key` 新任务登记而自动撤销。

在 Folia 上，普通任务的延迟与间隔按实体所在区域的 tick 计算，后台任务按全局 tick 等待。每轮回调结束后才开始等待下一轮的间隔；间隔非正时连续执行，不额外等待 tick。

`async = true` 的正文在后台执行，不能直接修改 Bukkit 实体；终止回调仍遵循上述清理线程规则。取消不会强制中断已经开始的回调，调用方需要协调仍在执行的后台工作。

正常停服时会取消注册表中的任务，并在插件关闭线程直接完成终止清理，包括此前已排队但尚未执行的清理回调。每个任务的终止回调最多执行一次。Folia 的停服清理不能依赖新提交的调度任务，也不能依赖当前区域数据或任意同步加载区块。运行期间实体调度器已退休时，清理回调可能无法立即执行，会保留到插件关闭时清理。

## 示例

::: code-group
```kotlin [Kotlin] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI
import org.bukkit.entity.Player

fun registerBleedTask() {
    EntityTaskAPI.registerTask(
        "example:bleed",
        task = { entity, params ->
            val damage = params["damage"] as? Double ?: return@registerTask
            (entity as? org.bukkit.entity.LivingEntity)?.damage(damage)
        }
    )
}

fun startBleed(player: Player, damage: Double) {
    EntityTaskAPI.runTaskKt(player, "example:bleed") {
        key = "example:bleed"   // 重复启动会替换上一次的流血
        delay = 20              // 1 秒后开始
        period = 20             // 每 1 秒一次
        count = 5               // 共 5 次
        params = mapOf<String, Any>("damage" to damage)
    }
}
```
```java [Java] :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public void registerBleedTask() {
    EntityTaskAPI.registerTask("example:bleed", (entity, params) -> {
        Object raw = params.get("damage");
        if (raw instanceof Double damage && entity instanceof LivingEntity living) {
            living.damage(damage);
        }
    });
}

public void startBleed(Player player, double damage) {
    EntityTaskAPI.runTask(player, "example:bleed", builder -> builder
        .key("example:bleed")   // 重复启动会替换上一次的流血
        .delay(20L)             // 1 秒后开始
        .period(20L)            // 每 1 秒一次
        .count(5)               // 共 5 次
        .params(java.util.Map.<String, Object>of("damage", damage))
    );
}
```
:::
