# 属性配置

<SyntaxBar
  label="配置位置"
  syntax="plugins/Assiya/attribute/**/*.yml"
/>

## 主属性 vs 仅读数属性

在 `attribute/` 下，顶层键名就是这条属性的**索引 ID**。你可以对比一下「带触发逻辑的主属性」与「只管读数的副属性」：

::: code-group
```yaml [带触发脚本的主属性 (crit)] :line-numbers
crit:
  names: ["暴击几率"]
  reader: percent
  handler: 'attribute/fight/crit'
  priority: 300
  triggers: attack
  options:
    attacker: '暴击! 对 {entity} 额外造成 {extra} 点暴击伤害, 总伤害 {damage}.'
    entity: '暴击! {attacker} 对你额外造成 {extra} 点暴击伤害, 总伤害 {damage}.'
```
```yaml [仅读取数值的副属性 (crit-rate)] :line-numbers
# 不写 handler 和 triggers 就不会有任何执行逻辑
# 只把数值读进来存好，供上面的 crit 脚本在计算公式时取用
crit-rate:
  names: ["暴伤倍率"]
  reader: default
```
:::

## 配置节点说明

<ParamList
  title="属性配置可用节点"
  :items="[
    { name: 'names', type: 'String | List<String>', default: '属性读取名', desc: '在 Lore 或命令文本中识别这条属性的名字，支持单个与多个', example: 'names: [物理伤害, 攻击力]' },
    { name: 'reader', type: 'String', default: '无', desc: '指定使用的读取组 <code>reader/</code>，不填则无法从文本或 NBT 中读取数值' },
    { name: 'triggers', type: 'String | List | Map', default: '无', desc: '属性会在哪些时机触发效果，也就是指定使用<code>触发器</code>' },
    { name: 'handler', type: 'String', default: '有 triggers 时必填', desc: '脚本相对于 <code>scripts/</code> 目录的路径，不需要写 <code>.groovy</code> 或 <code>.js</code> 后缀', example: 'handler: attribute/fight/attack' },
    { name: 'priority', type: 'Int', default: '0', desc: '相同触发器的执行顺序，<b>数值越小越先执行</b>' },
    { name: 'period', type: 'Long (tick)', default: 'timer 触发器必填', desc: '指定 timer 触发器的执行间隔' },
    { name: 'always-run', type: 'Boolean', default: 'false', desc: '设为 <code>true</code> 时跳过判断「实体是否持有该属性」，让脚本无论如何都会执行' },
    { name: 'options', type: 'Map', default: '无', desc: '传给脚本自行读取的自定义配置项（比如战斗提示或者吸血附加伤害的开关）' }
  ]"
/>

## `triggers` 的三种写法

当一条属性需要绑定多个触发器时，既可以简写共用外层配置，也可以展开为每个触发器单独指定 `priority` 和 `handler`（没写的字段会自动继承外层节点）：

::: code-group
```yaml [1. 单个触发器]
physical-attack:
  names: ["物理伤害", "攻击力"]
  reader: default
  handler: 'attribute/fight/attack'
  priority: 100
  triggers: attack
```
```yaml [2. 多触发器简写]
example:
  names: ["测试属性"]
  reader: default
  handler: 'attribute/my-handler'
  priority: 100
  triggers: ['attack', 'kill']
```
```yaml [3. 多触发器独立配置]
example:
  names: ["测试属性"]
  reader: default
  priority: 100
  always-run: false
  triggers:
    attack:
      handler: 'attribute/fight/attack'
    kill:
      priority: 200
      handler: 'attribute/kill-example'
```
:::

## 为什么要开 `always-run: true`？

默认情况下，触发器执行前会先过滤掉触发者身上没有的属性，不让它执行脚本。

但如果像 `max-health`（生命上限）或 `moving`（移动速度）这种常驻加成属性不开这个选项，会发生什么？

<FlowSteps
  :columns="3"
  :steps="[
    { tag: '步骤 1', title: '手持 +100 血量的剑', desc: '<code>max-health</code> 触发，脚本给玩家加上了 100 点原版血量上限。' },
    { tag: '步骤 2', title: '切换到空手', desc: '此时玩家身上已经没有 <code>max-health</code> 属性了。' },
    { tag: '步骤 3', title: '清理旧加成', desc: '若未开启 <code>always-run</code>，切空手时不会执行脚本，之前加上的 100 点血就扣不掉了；开启后脚本照常执行，发现属性为空时把旧血量删掉。' }
  ]"
/>