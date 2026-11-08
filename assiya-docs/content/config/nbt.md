# 在 NBT 中编写属性

鉴于 NBT 层级较深，建议通过物品库插件在配置文件中编写，而不是用游戏内命令手搓。

::: info 前置说明与知识储备
鉴于 NBT 跨版本兼容要做的事情太多，Assiya 本体不提供底层 NBT 解析，目前内置了基于 **[NeigeItems](https://github.com/ankhorg/NeigeItems-Kotlin)** 的读取实现（安装后自动启用）。如果你需要对接其他物品库或 PDC 数据，可以查阅 [对外接口与事件 · 注册自定义物品读取器](../advanced/api.md#常见对接代码示例)。

如果你只是想先让一件物品提供属性，建议先阅读 [为物品添加属性](../guide/quick-start.md)。在编写 NBT 属性前，请确保你已经了解：
- [属性配置](./attributes.md)：什么是属性的`索引 ID`（而非中文`读取名`）。
- [读取组与自定义格式](./readers.md#读取组芝士之捕获组-captures-保存读到的值)：属性使用的`读取组`声明了哪些`捕获组`（`captures`）。
- [默认属性与条件](./defaults.md#默认条件) / [使用条件与生效范围](./conditions.md)：条件的 `key` 与所需参数。
:::

## 一、NBT 整体结构

Assiya 读取物品根标签下的 `assiya` compound，其下包含两个顶层节点（两个节点都为空时，这件物品不会产生 Assiya 属性数据）：

<ParamList
  title="assiya 顶层节点说明"
  :items="[
    { name: 'attributes', type: 'Compound', desc: '声明这件物品提供的属性数据' },
    { name: 'conditions', type: 'List<Compound>', desc: '决定哪些路径上的属性或单个捕获组生效' }
  ]"
/>

这里以 NeigeItems 物品库的配置为例，展示一份包含多条属性与局部条件限制的完整配置：

```yaml [NeigeItems 物品配置] :line-numbers
assiya-nbt-example:
  material: IRON_SWORD
  name: '&f这是一把村里最好的剑'
  // [!code focus:20]
  nbt:
    assiya:
      attributes:
        bonus:
          physical-attack:
            value: (Int) 10
            percent: (Int) 15
          max-health:
            value: (Int) 20
      conditions:
        - paths:
            - bonus.physical-attack.percent
          conditions:
            - key: level-require
              value: (Int) 30
            - key: permission
              value: warrior
```

## 二、`attributes` 的三层结构与值转换

```text
attributes
└─ bonus                 ->  第 1 层：属性组 ID（自定义分组名，用于拼出条件路径）
   ├─ physical-attack    ->  第 2 层：属性索引 ID（注意不是中文读取名 names）
   │  ├─ value: 10       ->  第 3 层：捕获组 ID（该属性所用读取组声明的 capture）
   │  └─ percent: 15     ->  第 3 层：捕获组 ID
   └─ max-health         ->  第 2 层：属性索引 ID
      └─ value: 20       ->  第 3 层：捕获组 ID
```

<FlowSteps
  :columns="3"
  :steps="[
    { tag: '第 1 层', title: '属性组 ID', sub: 'bonus', desc: '用于给属性分组（只要不带 <code>.</code> 号即可随意命名）。Assiya 会用 <code>属性组ID.属性ID</code> 作为来源路径，方便在条件中按分组或单条属性精确控制生效范围' },
    { tag: '第 2 层', title: '属性索引 ID', sub: 'physical-attack', desc: '必须填写 <code>attribute/</code> 配置文件中的顶层<b>索引 ID</b>，不能填中文读取名（如 <code>攻击力</code>）。未注册的属性 ID 会被直接跳过' },
    { tag: '第 3 层', title: '捕获组键值对', sub: 'value / percent', desc: '键名必须是该属性绑定的读取组在 <code>captures</code> 中声明过的捕获组名；未在读取组中声明的键会被忽略' }
  ]"
/>

::: tip 捕获组的值是如何转换的？
Assiya 根据属性绑定的[读取组](./readers.md#读取组芝士之捕获组-captures-保存读到的值)配置来转换每个捕获组的值：
- **数值直转**：如果 NBT 中的值本身是数字（如 `(Int) 10` 或 `10`），且该捕获组使用数值合并方式（`add`、`multiply`、`max`、`min`），Assiya 会直接将其转为 `Double` 数值。
- **格式解析**：其他情况下（例如在 NBT 中写了字符串 `"IV"` 或 `"战士/法师"`），会先转为文本，再交给该捕获组配置的 `format`（如 `roman`、`text`、`decimal`）解析。
:::

## 三、`conditions` 与路径粒度 (`paths`)

`conditions` 是一个列表，每条记录由 `paths`（条件不满足时要剔除的路径列表）与 `conditions`（需要**同时满足**的条件列表）组成：

<ParamList
  title="条件记录字段说明"
  :items="[
    { name: 'paths', required: true, type: 'List<String>', desc: '指定这组条件管控哪些属性路径。当同组的 <code>conditions</code> 中有<b>任意一个</b>不满足时，<code>paths</code> 列出的路径会被剔除' },
    { name: 'conditions', required: true, type: 'List<Compound>', desc: '需要同时满足的条件列表。每个条件必须包含 <code>key</code>（条件索引 ID，如 <code>level-require</code>、<code>permission</code>），其余键值对会按该条件的捕获组读取并传入条件脚本（如 <code>value: 30</code>，或区间等级的 <code>min: 10</code> 与 <code>max: 20</code>）' }
  ]"
/>

路径由 `属性组 ID`、`属性索引 ID` 和 `捕获组 ID` 使用 `.` 拼接而成，写到哪一层就管到哪一层：

<ParamList
  title="paths 四种路径粒度对比"
  :items="[
    { name: 'paths: []', type: '整个 NBT 来源级', desc: '路径列表留空时，只要条件不满足，整个 NBT 属性源全部失效。' },
    { name: 'bonus', type: '属性组级', desc: '条件不满足时，移除整个 <code>bonus</code> 分组下的所有属性，其他分组不受影响。' },
    { name: 'bonus.physical-attack', type: '单条属性级', desc: '条件不满足时，仅移除 <code>bonus</code> 组下的 <code>physical-attack</code> 属性，同组的 <code>max-health</code> 照常生效。' },
    { name: 'bonus.physical-attack.percent', type: '单个捕获组级', desc: '条件不满足时，<b>只移除 <code>percent: 15</code> 这一个捕获值</b>，同一条属性下的 <code>value: 10</code> 与同组的 <code>max-health</code> 仍然保留生效。' }
  ]"
/>

以第一节的完整配置为例，当一名 **20 级的战士** 手持该武器时（满足 `permission: warrior`，但不满足 `level-require: 30`），各条数据的最终结算状态如下：

```yaml :line-numbers
assiya:
  attributes:
    bonus:
      physical-attack:
        // [!code ++]
        value: 10       # 保留生效（不受 bonus.physical-attack.percent 路径限制）
        // [!code --]
        percent: 15     # 被移除（等级未达到 30 级）
      max-health:
        // [!code ++]
        value: 20       # 保留生效
```

::: tip 基于 NBT 不可见特性衍生的效果
与直观展示给玩家的 Lore 不同，客户端默认看不到物品的 NBT 数据。除了方便物品库或脚本自动生成结构化属性外，你还可以用它实现：
- **隐藏属性彩蛋**：偷偷在特定装备里藏一点额外属性，不在 Lore 上写出来。
- **表面词条与实际属性分离**：在 Lore 里写忽悠玩家的酷炫词条描述（不匹配任何读取组），而在 NBT 里写入实际生效的 `physical-attack` 或自定义属性。
- **盲盒 / 暗数值词条**：Lore 只展示「攻击力提升（中）」，具体加多少数值藏在 NBT 中生效。
:::

## 四、常见避坑与排错指南

### 1. 不能把属性直接写成一个数字

属性节点下必须是一个包含捕获组键值对的 compound，不能跳过捕获组直接写数值：

::: code-group
```yaml [正确的写法]
assiya:
  attributes:
    bonus:
      // [!code ++:2]
      physical-attack:
        value: (Int) 10
```
```yaml [错误的写法]
assiya:
  attributes:
    bonus:
      // [!code error]
      physical-attack: (Int) 10
```
:::

### 2. 写了 `assiya` 标签但属性没有生效？

手持或穿戴物品执行 `/assiya status <玩家名>` 若看不到预期属性，可按以下四步依次排查：

<FlowSteps
  :columns="2"
  :steps="[
    { tag: '排查 1', title: '检查根标签与前置插件', sub: 'Root -> assiya', desc: '确认服务器已安装并启用 <b>NeigeItems</b>，且 <code>assiya</code> 直接位于物品 NBT 的根标签下（没有被包在其他自定义节点里面）。' },
    { tag: '排查 2', title: '核对索引 ID（非中文名）', sub: 'physical-attack', desc: '确认属性第 2 层与条件 <code>key</code> 填写的是配置文件顶层的<b>索引 ID</b>（如 <code>physical-attack</code>、<code>level-require</code>），而不是 <code>names</code> 里的中文读取名，且属性组 ID 中没有包含 <code>.</code> 号。' },
    { tag: '排查 3', title: '核对捕获组名称', sub: 'value / percent / min / max', desc: '确认属性第 3 层和条件的参数键名（如 <code>value</code>）确实存在于该属性或条件所绑定的 <code>reader</code> 的 <code>captures</code> 列表中。' },
    { tag: '排查 4', title: '检查条件是否拦截', sub: 'conditions -> paths', desc: '对照当前玩家的等级、权限或穿戴槽位，检查是否因为某条 <code>conditions</code> 未通过而把对应路径剔除了。' }
  ]"
/>
