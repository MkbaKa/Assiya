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
| `count` | `1` | 执行次数；为 `null` 时无限循环 |
| `async` | `false` | 是否在异步线程执行 |
| `params` | 空 | 传给 `task` 与 `onTerminate` 的运行时数据 |

## 取消与终止

| 方法 | 说明 |
| --- | --- |
| `cancelTask(owner, key)` / `cancelTask(ownerUUID, key)` | 取消指定实体上指定 `key` 的任务 |
| `EntityTask.cancel()` | 取消单个任务实例 |

任务被取消或被同 `key` 的新任务替换时从注册表移除。自然执行完毕不会触发 `onTerminate`；只有被终止（取消）时才触发。实体退出或移出世界时，其全部任务会被取消。

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
