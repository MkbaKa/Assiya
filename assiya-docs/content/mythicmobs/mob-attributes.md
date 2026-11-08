# 怪物属性与等级公式

安装 MythicMobs 后，可以在怪物配置文件（`Mobs/*.yml`）下直接用 `attributes` 列表给怪物添加属性。怪物生成时会自动获得名为 `assiya:mythic-mobs:attributes` 的属性源（优先级在 `config.yml` 的 `mythicmobs.source-priority` 中设置，默认 `100`）。

## 固定属性与等级计算

::: code-group
```yaml [1. 固定数值属性] :line-numbers
test_zombie:
  Type: ZOMBIE
  Display: "&7测试僵尸"
  Health: 200
  // [!code focus:4]
  attributes:
    - "物理伤害: 40"
    - "物理防御: 15"
    - "暴击几率: 20"
```
```yaml [2. 随怪物等级动态成长 (calculator-attribute)] :line-numbers
skeleton_king:
  Type: SKELETON
  Display: "&c骷髅王 Lv.<caster.level>"
  Health: 500
  // [!code focus:6]
  calculator-attribute: true
  attributes:
    - "物理伤害: ${50 + <level> * 15}"
    - "护甲: ${min(80, 10 + <level> * 2)}"
    - "暴击几率: 25"
    - "暴伤倍率: ${120 + <level> * 3}"
```
:::

<ParamList
  title="怪物配置节点说明"
  :items="[
    { name: 'attributes', required: true, type: 'List<String>', desc: '怪物出生时获得的属性' },
    { name: 'calculator-attribute', default: 'false', type: 'Boolean', desc: '开启后，可以在属性文本中使用 <code>${算式}</code> 计算数值，算式内使用 <code>&lt;level&gt;</code> 代表怪物的当前等级，支持 <code>min</code> / <code>max</code> / <code>random</code> 之类的函数。' }
  ]"
/>