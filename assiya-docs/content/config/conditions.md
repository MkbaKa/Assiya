# 使用条件与生效范围

<SyntaxBar
  label="配置位置"
  syntax="plugins/Assiya/condition/**/*.yml"
/>

使用条件用来限制某件物品（或者某一行属性）能不能在实体身上生效。

下面的框框里可以测试条件效果：

<ConditionDemo
  :level-range="{ min: 1, max: 60 }"
  :initial-level="8"
  initial-slot="MAIN_HAND"
  :initial-perms="['战士']"
  :available-perms="['战士', '圣骑士', '法师', 'vip', 'svip']"
  :initial-lines="[
    '等级需求: 5',
    '装备槽位: 主手/副手',
    '攻击力: 100',
    '真实伤害: 20 等级需求: 10',
    '暴击几率: 30 职业: 圣骑士/法师'
  ]"
  :attributes="[
    { id: 'physical-attack', names: ['物理伤害', '攻击力'], label: '攻击力' },
    { id: 'pvp-attack', names: ['PVP伤害'], label: 'PVP伤害' },
    { id: 'pve-attack', names: ['PVE伤害'], label: 'PVE伤害' },
    { id: 'real-attack', names: ['真实伤害'], label: '真实伤害' },
    { id: 'physical-defense', names: ['物理防御', '防御力'], label: '物理防御' },
    { id: 'max-health', names: ['生命上限', '生命力'], label: '生命力' },
    { id: 'crit', names: ['暴击几率'], label: '暴击几率' },
    { id: 'crit-rate', names: ['暴伤倍率'], label: '暴伤倍率' }
  ]"
  :conditions="[
    { id: 'level-require', names: ['等级需求', 'Lv.'], reader: 'default' },
    { id: 'permission', names: ['权限需求', '职业'], reader: 'text' },
    { id: 'equipment-slot', names: ['装备槽位', '装备类型'], reader: 'text' }
  ]"
  :slots="[
    { key: 'MAIN_HAND', label: '主手', matches: ['主手', '双持'] },
    { key: 'OFF_HAND', label: '副手', matches: ['副手', '双持'] },
    { key: 'HEAD', label: '头盔', matches: ['头盔'] },
    { key: 'CHEST', label: '胸甲（衣服）', matches: ['衣服'] },
    { key: 'LEGS', label: '护腿（裤子）', matches: ['裤子'] },
    { key: 'FEET', label: '靴子（鞋子）', matches: ['鞋子'] }
  ]"
/>

## 整件物品条件 vs 单行条件

如果条件所在的一行文本中没有属性存在，那么它会被作为整件物品的条件。

反之，同行存在属性，那就只对这一条属性生效：

<McTooltip
  name="&f测试铁剑"
  :lines="[
    { text: '&7等级需求: 5', note: '本行没有属性 -> 整件物品条件', status: 'warn' },
    { text: '&7装备槽位: 主手/副手', note: '本行没有属性 -> 整件物品条件', status: 'warn' },
    { text: '&c攻击力: 100', note: '无条件属性', status: 'ok' },
    { text: '&b真实伤害: 20 &7等级需求: 10', note: '同行有属性 -> 只影响这 20 点真伤', status: 'ok' }
  ]"
/>

<FlowSteps
  :columns="2"
  :steps="[
    { tag: '整件物品', title: '单独成行', desc: '不满足条件则整件物品的属性都不能用' },
    { tag: '单行属性', title: '同行混写', desc: '不满足条件只影响这一行属性' }
  ]"
/>

## 条件配置写法

```yaml [condition/example.yml] :line-numbers
level-require:
  names: ["等级需求", "Lv."]
  reader: default
  handler: 'condition/level-require'

permission:
  names: ["权限需求", "职业"]
  reader: text
  handler: 'condition/permission'

equipment-slot:
  names: ["装备槽位", "装备类型"]
  reader: text
  handler: 'condition/equipment-slot'
  options:
    MAIN_HAND: ['主手', '双持']
    OFF_HAND: ['副手', '双持']
    HEAD: ['头盔']
    CHEST: ['衣服']
    LEGS: ['裤子']
    FEET: ['鞋子']
```

<ParamList
  title="条件配置节点说明"
  :items="[
    { name: 'names', required: true, type: 'List<String>', desc: '读取名' },
    { name: 'reader', required: true, type: 'String', desc: '读取组' },
    { name: 'handler', required: true, type: 'String', desc: '条件判断脚本路径' },
    { name: 'options', type: 'Map', desc: '传给条件脚本读取的额外配置' }
  ]"
/>
