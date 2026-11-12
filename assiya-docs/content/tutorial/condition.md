# 创建一个新的条件

在[前文中](../config/defaults.md#默认条件)我们列出了 Assiya 自带的几种基础条件，我认为它能覆盖大部分需求。

但如果你想学习并搞清楚如何编写一个条件，本文将会以 `level-require` 即实现一个等级需求的过程来教会你。

## 让 Assiya 认识这个属性

同样的，增加一个条件也需要在配置里定义，并给它一个读取组用于解析文本。

这里直接给出完整配置不多赘述：

```yaml
level-require:
  names: ["等级需求", "Lv."]
  reader: default
  handler: 'condition/level-require'
```

相比于定义一个属性，条件写起来会简单很多，它只需要读取名、读取组以及条件逻辑脚本三个节点。

## 条件逻辑

条件的运行方式没有属性的触发器、优先级等等那么多弯弯绕绕，只需要一个函数即可：

```groovy
@CompileStatic
static boolean check(Condition condition, Entity entity, ConditionContextImpl context) {
    return true // 条件是否满足
}
```

函数返回 true 代表被条件限定的属性可以使用，反之返回 false 代表无法使用。

和读取组一样，编写条件之前同样需要搞清楚自己的需求。**你想让这个条件限制什么？**

- 我想要限制玩家的原版经验等级；那就从 `Player#getLevel` 方法获得数据。
- 我想要玩家拥有权限才能通过；那就使用 `Player#hasPermission` 方法判断权限。
- 我想要带有该条件的物品在特定槽位才能使用；这个比较复杂，我建议去看 `equipment-slot` 条件的实现。

那么本文的例子假设 `level-require` 这一条件并不存在，而我们想要实现玩家等级的判断。

基于这一前提，可以总结出以下几点：

- 条件执行时需要过滤掉非玩家的实体，因为它们并没有等级。
- 执行时要获得这个条件限制的具体数值。
- [获得玩家等级](https://jd.papermc.io/paper/26.3/org/bukkit/entity/Player.html#getLevel())，与限制值进行比较。

实现出来的代码就是：

```groovy
@CompileStatic
static boolean check(Condition condition, Entity entity, ConditionContextImpl context) {
    // 若被判断的实体不是玩家 直接让它符合条件
    if (!entity instanceof Player) return true

    // 转换类型为玩家
    Player player = entity as Player
    
    // 取得条件限制的值
    // 如 "等级需求 250" 中的 250
    // 这里传进 capture 方法的文本就是捕获组的名称
    int require = context.capture("value")
    // 拿玩家等级和值进行比较
    return player.getLevel() >= require
}
```
