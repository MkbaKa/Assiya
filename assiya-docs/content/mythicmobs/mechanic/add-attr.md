# 给予属性 AddAttribute

<SyntaxBar
  label="技能示例"
  syntax="- assiya:add-attr{s=来源名称;d=时长;属性项...} @target"
  :aliases="['assiya:add-attr', 'assiya:addattr']"
  badge="实体目标技能"
/>

给技能目标增加一个属性源（目标身上同名 `source` 会被覆盖）

<MmLineBuilder
  :targeters="['@target', '@self', '@EIR{r=5}', '@PIR{r=10}', '@trigger', '@owner']"
  title="给予属性"
  skill="assiya:addattr"
  targeter="@self"
  :fields="[
    { id: 's', key: 'source', label: '属性源', default: 'boss_rage', placeholder: '如 boss_rage' },
    { id: 'd', key: 'duration', label: '持续时间', type: 'number', default: 100 },
    { id: 'p', key: 'priority', label: '优先级', type: 'number', default: 200, omitIf: 200 },
    { id: 'attrs', label: '要增加的属性', default: '攻击力=50%;暴击几率=30', raw: true, wide: true }
  ]"
/>

除了下方 3 个参数外，剩余所有的参数都会被当作属性词条解析：

<ParamList
  title="技能参数"
  :items="[
    { name: 'source', aliases: ['s'], default: '随机 UUID', type: 'String', desc: '属性源名称' },
    { name: 'duration', aliases: ['d'], default: '-1', type: 'Long (tick)', desc: '持续时间，<b>单位为 tick</b>（20 tick = 1 秒）。填 <code>-1</code> 表示持续到死亡/下线' },
    { name: 'priority', aliases: ['p'], default: '200', type: 'Int', desc: '该属性源的合并优先级' }
  ]"
/>

## 例子

要增加的属性既可以写属性的`索引名`，也可以写`读取名`，就像 `physical-attack` 与 `物理伤害`，它们本身都是同一个属性。

::: code-group
```yaml [按属性读取名或算式匹配]
Skills:
  # 给自身添加持续 100 tick (5秒) 的 boss_rage 属性源
  - assiya:add-attr{s=boss_rage;d=100;physical-attack=50%;暴击几率=30} @self

  # 等号右侧支持写算式（以及 MM 占位符）
  - assiya:add-attr{s=level_buff;d=200;物理伤害=20+<caster.level>*5} @self
```
```yaml [用 | 直接指定捕获组]
Skills:
  # 格式为 "属性 id 或读取名|捕获名=数值", 跳过正则匹配直接往对应 capture 塞值
  # 堆在一起可能不太好读 实际上就是：
  # 物理伤害|min=80   ->  80
  # physical-attack|max=120  ->  120
  # 效果为 "物理伤害 80 - 120"
  - assiya:add-attr{s=buff_1;d=200;物理伤害|min=80;physical-attack|max=120} @target
```
:::
