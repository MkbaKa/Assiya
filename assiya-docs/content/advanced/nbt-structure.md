# Assiya 的 NBT 结构

NBT 读取适合给物品写入玩家不可见的属性数据，它不需要经过 Lore 文本匹配，而是直接读取物品上的 `assiya` 标签。

如果你只是想先让一件物品提供属性，建议先阅读[物品属性](../getting-start/item-attribute.md)。  
鉴于 NBT 的结构比较复杂，建议通过物品库插件在配置中编辑，而不是通过游戏命令手动编辑。

基于 NBT 编写属性需要的知识储备较高，需要在阅读 [属性 id](TODO) 与 [capture](TODO) 并熟悉 NBT 结构等概念后才可以上手。

## 前置条件

鉴于 NBT 跨版本兼容要做的事情太多，Assiya 选择不提供内置的解析逻辑。   
而是通过依赖其他插件进行 NBT 读取实现。  
所以如果想通过 NBT 来编辑物品的 Assiya 属性，需要安装前置插件。

目前 Assiya 内只有一个基于 [NeigeItems](https://github.com/ankhorg/NeigeItems-Kotlin) 插件的读取实现。  
如果你想扩展读取实现，可以阅读[附属开发-物品读取](TODO)页面。

## NBT 的格式是什么？

Assiya 读取物品根标签下的 `assiya` compound，  
这里以 NeigeItems 物品库的配置为例：

```yaml :line-numbers
assiya-nbt-example:
  material: IRON_SWORD
  name: '&f这是一把村里最好的剑'
  nbt:
    assiya:
      attributes:
        bonus:
          physical-attack:
            value: (Int) 10
      conditions:
        - paths:
            - bonus.physical-attack
          conditions:
            - key: level-require
              value: (Int) 10
```

看起来可能有点复杂？让我们一步步拆解开学习。

`assiya` 下有两个节点：

| 节点 | 类型 | 作用 |
|-|-|-|
| `attributes` | compound | 声明这件物品提供的属性 |
| `conditions` | list | 决定哪些属性或 capture 是否生效 |

两个节点都没有内容时，这件物品不会产生 Assiya 属性数据。

## attributes 应该怎么写?

`attributes` 使用两层 compound：

```yaml :line-numbers
assiya:
  attributes:
    bonus:
      physical-attack:
        value: 10
        percent: 15
```

这段结构可以理解为：

```text
attributes
└─ bonus                 ->  属性组 id
   └─ physical-attack    ->  属性 id
      ├─ value: 10       ->  capture
      └─ percent: 15     ->  capture
```

### 第一层是属性组 id

```yaml :line-numbers
assiya:
  attributes:
    // [!code focus]
    bonus: 
      physical-attack:
        value: 10
        percent: 15
```

`bonus` 是一个属性组的名字，顾名思义，它的用处是给属性分组，方便做一些复杂操作。

Assiya 会用 `属性组 id.属性 id` 作为这条属性在该来源中的路径。 上面的属性路径就是 `bonus.physical-attack`。

当条件需要指定属性路径的时候，就可以使用 `bonus.physical-attack` 来代指这条属性。

如果条件只影响某个 capture，可以在路径后继续写 capture id，比如 `bonus.physical-attack.percent`。

### 第二层是属性 id

```yaml :line-numbers
assiya: 
  attributes:
    bonus:
      // [!code focus]
      physical-attack: 
        value: 10
        percent: 15
```

`physical-attack` 是属性 id。注意，不是属性配置中的 `names` 内容，是更上层的 `索引 id`。

### 第三层是 capture

```yaml :line-numbers
assiya:
  attributes:
    bonus:
      physical-attack:
        // [!code focus]
        value: 10
        // [!code focus]
        percent: 15   
```

`value` 和 `percent` 都是 capture id，它们必须是该属性使用的 reader 所声明的 capture。

capture 的值就是这条属性提供给 reader 的原始数据：

```yaml
physical-attack:
  value: 10
  percent: 15
```

Assiya 会按照对应 reader 的 capture 配置读取这些值，没有写在配置里的 capture 会被忽略。

## 一条属性从 NBT 到结果经历了什么？

Assiya 读取 NBT 时，会按下面的顺序处理：

1. 读取物品上的 `assiya` compound。
2. 读取 `attributes` 和 `conditions`。
3. 将 `attributes` 的每个第一层节点转换成属性组。
4. 根据属性 id 找到对应的属性和 reader。
5. 根据 reader 的 capture 配置转换每个 capture 的值。
6. 将属性组和条件交给属性解析器。
7. 条件通过后，才把属性贡献加入实体的属性结果。

## capture 的值怎么转换？

如果 NBT 中的值本身是数字，且该 capture 使用数值操作， Assiya 会直接把它转换为数值。

其他情况会先转成文本，再交给 capture 的 format 解析。

因此，capture 能否读取某个值，取决于属性使用的 reader 配置， 不只是看 NBT 中的字段名称。

## conditions 应该怎么写？

`conditions` 是一个列表，每个列表元素包含两个节点：

```yaml :line-numbers
assiya:
  conditions:
    - paths:
        - bonus.physical-attack.percent
      conditions:
        - key: level
          min: 30
```

| 节点 | 类型 | 作用              |
|-|-|-----------------|
| `paths` | list | 条件不满足时，要阻止的属性路径 |
| `conditions` | list | 需要同时满足的条件       |

条件项中的 `key` 是条件 id，其余键是传给条件的参数。  

上面的例子表示：当 `level` 满足时，`bonus.physical-attack.percent` 才会被添加到可用的属性中。

也就是当玩家在 `30` 级以后才能获得 `15` 点 `physical-attack percent`。

### paths 写到哪里？

路径由属性组 id，属性 id 和 capture id 组成，使用 `.` 分隔：

```text
bonus
bonus.physical-attack
bonus.physical-attack.percent
```

它们分别表示不同范围：

- `bonus`：影响整个 `bonus` 属性组。
- `bonus.physical-attack`：影响 `physical-attack` 这条属性。
- `bonus.physical-attack.percent`：只影响 `percent` 这个 capture。

### 多个条件怎么处理?

同一个条件记录中的 `conditions` 会同时参与判断。

只有全部条件通过，这条路径上的属性才可以加载。

```yaml
assiya:
  conditions:
    - paths:
        - bonus.physical-attack
      conditions:
        - key: level
          min: 30
        - key: job
          value: warrior
```

上面的写法要求 `level` 和 `job` 两个条件都通过，任意一个条件不满足，`bonus.physical-attack` 都会被移除。

## 一个完整示例

```yaml :line-numbers
assiya:
  attributes:
    bonus:
      physical-attack:
        value: 10
        percent: 15
      max-health:
        value: 20
  conditions:
    - paths:
        - bonus.physical-attack.percent
      conditions:
        - key: level
          min: 30
```

这件物品包含三条 capture：

- `bonus.physical-attack.value` 提供 `10`。
- `bonus.physical-attack.percent` 提供 `15`，但需要 `level` 条件通过。
- `bonus.max-health.value` 提供 `20`。

如果 `level` 条件不满足，只会移除 `bonus.physical-attack.percent`。 `bonus.physical-attack.value` 和 `bonus.max-health.value` 仍然保留。

## NBT 结构和 Lore 有什么区别?

Lore 是把属性写成玩家能看到的文本，Assiya 需要按 reader 的规则匹配文本。

NBT 是把属性 id 和 capture 直接写入物品标签，玩家通常看不到这些数据。

因此：

- Lore 更适合手动编辑和展示属性。
- NBT 更适合物品库、脚本或其他插件自动生成。
- 两种来源都通过 Assiya 的属性定义和 reader 进入后续解析。

基于玩家不可见的特性，还可以衍生出这种用法：

- 偷偷在物品里藏一点属性，不告诉玩家。
- 在 Lore 里写点忽悠玩家的高端属性名，但 NBT 里实际生效的是普通的 physical-attack。
- 玩家看不到具体属性值的词条属性。

## 常见问题

### 写了 `assiya` 但没有属性？

1. `assiya` 是否是物品根标签下的 compound。
2. `attributes` 里的 “属性组 id . 属性 id . capture” 有没有打错。
3. capture id 在不在属性的 reader 配置里。
4. 是不是没满足使用条件？

### 可以随便写属性组 id 吗？

可以，属性组 id 只用于构成来源路径，只要里面不带 `.` 符号被 yaml 当成下级节点就随便写。

### 可以把 capture 写成一个数字吗？

不可以。属性条目必须是一个 compound，capture 作为它下面的键和值存在。

```yaml
// [!code ++]
# 正确
  // [!code ++]
attack:
  // [!code ++]
  value: 10 

// [!code --]
# 错误
// [!code --]
attack: 10
```
