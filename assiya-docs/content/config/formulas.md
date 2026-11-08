# 调整计算公式

<SyntaxBar
  label="配置位置"
  syntax="plugins/Assiya/formula/**/*.yml"
/>

默认属性中，大部分属性效果的计算公式都可以在 `formula` 即公式配置里修改。

当然，在编写脚本时写死在代码里也没人管你。

下面的框框是一个可以用来测试公式效果的计算工具，可以随便修改公式、数值来测试你想要的效果。

也可以随便增加、删掉某个变量：

<FormulaDemo
  :result-labels="{
    armor: '实际减少的伤害',
    crit: '公式计算结果'
  }"
  :effects="{
    armor: [
      { label: '实际减伤比例', expression: '<result> / <damage>', format: 'percent' },
      { label: '剩余伤害', expression: '<damage> - <result>', format: 'number' }
    ],
    crit: [
      { label: '暴击后总伤害', expression: '<damage> + <result>', format: 'number' }
    ]
  }"
  :presets="[
    {
      label: '默认护甲（百分比线性减伤）',
      formulaId: 'armor',
      expression: 'max(0, <damage> * (<armor> - <penetration>) / 100)',
      vars: { damage: 200, armor: 60, penetration: 15 },
      varMeta: [
        { key: 'damage', label: '当前总伤害', min: 10, max: 1000, step: 10 },
        { key: 'armor', label: '受击方护甲', min: 0, max: 300, step: 5 },
        { key: 'penetration', label: '攻击方护甲穿透', min: 0, max: 200, step: 5 }
      ]
    },
    {
      label: '示例 1：限制最高减免 80%',
      formulaId: 'armor',
      expression: '<damage> * min(0.8, max(0, <armor> - <penetration>) / 100)',
      vars: { damage: 200, armor: 120, penetration: 15 },
      varMeta: [
        { key: 'damage', label: '当前总伤害 <damage>', min: 10, max: 1000, step: 10 },
        { key: 'armor', label: '受击方护甲 <armor>', min: 0, max: 300, step: 5 },
        { key: 'penetration', label: '攻击方护甲穿透 <penetration>', min: 0, max: 200, step: 5 }
      ]
    },
    {
      label: '示例 2：MOBA 边际递减护甲',
      formulaId: 'armor',
      expression: 'max(0, <damage> * max(0, <armor> - <penetration>) / (max(0, <armor> - <penetration>) + 100))',
      vars: { damage: 200, armor: 100, penetration: 0 },
      varMeta: [
        { key: 'damage', label: '当前总伤害 <damage>', min: 10, max: 1000, step: 10 },
        { key: 'armor', label: '受击方护甲 <armor>', min: 0, max: 300, step: 5 },
        { key: 'penetration', label: '攻击方护甲穿透 <penetration>', min: 0, max: 200, step: 5 }
      ]
    },
    {
      label: '默认暴击（暴伤加成）',
      formulaId: 'crit',
      expression: 'max(0, <damage> * (<rate> - <resist>) / 100)',
      vars: { damage: 200, rate: 80, resist: 20 },
      varMeta: [
        { key: 'damage', label: '当前总伤害 <damage>', min: 10, max: 1000, step: 10 },
        { key: 'rate', label: '攻击方暴伤倍率 <rate>', min: 0, max: 300, step: 5 },
        { key: 'resist', label: '受击方暴伤抵抗 <resist>', min: 0, max: 200, step: 5 }
      ]
    }
  ]"
/>

## 语法与内置函数

<ParamList
  title="公式支持的语法元素"
  :items="[
    { name: '<变量名>', type: '变量引用', desc: '引用由脚本传入的变量（如 <code>&amp;lt;damage&amp;gt;</code>、<code>&amp;lt;armor&amp;gt;</code>、<code>&amp;lt;penetration&amp;gt;</code>）' },
    { name: '+  -  *  /  %  ^  ()', type: '运算符', desc: '支持四则运算、取余 <code>%</code>、乘方 <code>^</code>（如 <code>2 ^ 3</code>）以及一元负号 <code>-&amp;lt;damage&amp;gt;</code>' },
    { name: 'max(a, b)', type: '内置函数', desc: '返回两个数中较大的一个。用来保底防止算出负数之类的', example: 'max(0, <armor> - <penetration>)' },
    { name: 'min(a, b)', type: '内置函数', desc: '返回两个数中较小的一个。用来限制减伤比例、伤害上限', example: 'min(0.8, <armor> / 100)' },
    { name: 'random(from, to)', type: '内置函数', desc: '返回 <code>[from, to)</code> 区间内的随机浮点数。', example: 'random(<min>, <max>)' }
  ]"
/>
