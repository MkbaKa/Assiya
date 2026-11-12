# 击杀触发

当被击杀的实体触发 `EntityDeathEvent` 时对击杀者执行，没什么好说的。

不过击杀触发在 MythicMobs v4.x 版本上存在一个兼容性问题，就像击杀触发器 `kill.groovy` 实现代码的注释中说的那样：

```groovy :line-numbers
/**
 * mm v4 可能是基于远古版本的 spigot 开发
 * 所以对 EntityDeathEvent 在 paper 是 Cancellable 这件事毫不知情
 * 于是其死亡监听器没有考虑实体似了又复活是怎么个事儿 并在延迟 60tick 后把它从存活人口里摘了出去
 *
 *  于是这个怪物就变成了没人要的野孩子: "原版怪"
 *
 * 而 v5 他们意识到了这个问题
 * 在监听器的开头加了一行
 * if (ServerVersion.isPaper() && event.isCancelled()) {
 *     return;
 * }
 * 该问题得以修复
 */
if (cancellable != null && cancellable.isCancelled() && kill.reviveHealth != null) {
    event.cancelled = true
    event.reviveHealth = Math.min(kill.reviveHealth, victim.maxHealth)
}
```

所以请不要在该版本环境未判断目标是否为 MythicMobs 或类似插件实体的情况下，操作 `TriggerByKillComponent` 的 `reviveHealth` 字段，除非你知道自己在做什么。
