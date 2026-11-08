<script setup>
const attributes = [
  ['physical-attack', '物理伤害 / 攻击力'],
  ['physical-defense', '物理防御 / 防御力'],
  ['max-health', '生命上限 / 生命力'],
  ['crit', '暴击几率'], ['crit-rate', '暴伤倍率'],
  ['armor', '护甲'], ['see-through', '护甲穿透'],
  ['dodge', '闪避几率'], ['hit', '命中几率'],
  ['vampire', '吸血几率'], ['moving', '移动速度'],
  ['health-regain', '生命恢复'], ['custom', '自定义']
]
const captures = [
  ['total', '最终值'],
  ['value', '固定值'], ['percent', '百分比'],
  ['min', '范围下限'], ['max', '范围上限'],
  ['random', '范围随机值'], ['rate', '百分比计算后的倍率']
]
const placeholders = [
  {
    path: ['attr'], label: '读取属性值',
    description: '读取玩家指定属性值。',
    params: [
      {
        key: 'attribute', name: '属性名', type: 'String', input: 'text',
        required: true, initial: 'physical-attack',
        presets: attributes.map(([value, label]) => ({ value, label })),
        description: '属性索引 ID 或者 读取名'
      },
      {
        key: 'capture', name: '捕获名', type: 'String', input: 'text',
        required: true, initial: 'total',
        presets: captures.map(([value, label]) => ({ value, label: value + ' · ' + label })),
        description: '读取组中的 <code>captures</code> 或 <code>placeholders</code>'
      },
      {
        key: 'fallback', name: '默认值', type: 'String', input: 'text',
        default: '', initial: '', placeholder: '留空使用初始值',
        description: '没有对应属性或读出空值时返回的内容'
      }
    ]
  },
  {
    path: ['in-combat'], label: '读取战斗状态',
    description: '返回玩家当前是否处在战斗状态：战斗中为 <code>true</code>，脱离战斗后为 <code>false</code>。',
    params: []
  }
]
</script>

# PlaceholderAPI 变量

<CommandTree
  root="%assiya"
  :commands="placeholders"
  separator="_"
  suffix="%"
  tree-title="变量结构"
  example-title="变量示例"
  params-title="变量参数"
  copy-label="复制"
/>
