<script setup>
const number = '([-+]?\\d+(?:\\.\\d+)?)'
const separator = '[\\s:：]*'
const rules = {
  range: { label: '<min>-<max>', regex: `${separator}${number}\\s*-\\s*${number}`, captures: ['min', 'max'] },
  percent: { label: '<percent>%', regex: `${separator}${number}\\s*%`, captures: ['percent'] },
  value: { label: '<value>', regex: `${separator}${number}`, captures: ['value'] }
}
</script>

# 读取组与自定义格式

<SyntaxBar
  label="配置位置"
  syntax="plugins/Assiya/reader/**/*.yml"
/>

读取组（`Reader`）用来决定怎么从一行文本中把数字或文字提取出来、重复读到同一条属性时怎么合并，以及怎么把提取出来的几个数算成最终结果。

## 默认配置提供的读取组

::: code-group
```yaml [default (普通的数值类解析规则)] :line-numbers
default:
  captures:
    value: add
    valueAddition: add
    min: add
    max: add
    percent: add
    percentAddition: add
    percentMin: add
    percentMax: add
    mul: multiply
  patterns:
    - '{name}[\s:：]*<percentMin>\s*-\s*<percentMax>\s*%'
    - '{name}[\s:：]*<min>\s*-\s*<max>'
    - '{name}[\s:：]*<percent>\s*\(\s*\+?\s*<percentAddition>\s*\)\s*%'
    - '{name}[\s:：]*<value>\s*\(\s*\+?\s*<valueAddition>\s*\)'
    - '{name}[\s:：]*<percent>\s*%'
    - '{name}[\s:：]*\*[\s:：]*<mul>'
    - '{name}[\s:：]*<value>'
    - '<percentMin>\s*-\s*<percentMax>\s*%\s*{name}'
    - '<min>\s*-\s*<max>\s*{name}'
    - '<percent>\s*\(\s*\+?\s*<percentAddition>\s*\)\s*%\s*{name}'
    - '<value>\s*\(\s*\+?\s*<valueAddition>\s*\)\s*{name}'
    - '<percent>\s*%\s*{name}'
    - '<mul>\s*\*\s*{name}'
    - '<value>\s*{name}'
  placeholders:
    random: 'random(<min>, <max>)'
    percentRandom: 'random(<percentMin>, <percentMax>)'
    rate: '(<percent> + <percentAddition> + <percentRandom>) / 100'
    valueRandom: '<value> + <valueAddition> + <random>'
    total: '<valueRandom> * (1 + <rate>) * <mul>'
```
```yaml [percent (百分比数值)] :line-numbers
percent:
  captures:
    value: add
    valueAddition: add
    min: add
    max: add
  patterns:
    - '{name}[\s:：]*<min>\s*-\s*<max>\s*%?'
    - '{name}[\s:：]*<value>\s*\(\s*\+?\s*<valueAddition>\s*\)\s*%?'
    - '{name}[\s:：]*<value>\s*%'
    - '{name}[\s:：]*<value>'
    - '<min>\s*-\s*<max>\s*%?\s*{name}'
    - '<value>\s*\(\s*\+?\s*<valueAddition>\s*\)\s*%?\s*{name}'
    - '<value>\s*%\s*{name}'
    - '<value>\s*{name}'
  placeholders:
    random: 'random(<min>, <max>)'
    valueRandom: '<value> + <valueAddition> + <random>'
    total: '<valueRandom>'
```
```yaml [roman (罗马数字)] :line-numbers
roman:
  captures:
    value:
      op: add
      format: roman
  patterns:
    - '{name}[\s:：]*<value>'
    - '<value>\s*{name}'
```
```yaml [text (纯文本)] :line-numbers
text:
  captures:
    value:
      op: last
      format: text
  patterns:
    - '{name}[\s:：]*<value>'
```
:::

### 一些简单的例子

| 读取组 | 数值写在后面          | 数值写在前面           |
| --- |-----------------|------------------|
| `default` | `物理伤害: 100`     | `100 物理伤害`       |
| `default` | `物理伤害: 100-200` | `100-200 物理伤害`   |
| `default` | `物理伤害: 50%`     | `50% 物理伤害`       |
| `default` | `物理伤害: 10-20%`  | `10-20% 物理伤害`    |
| `default` | `物理伤害: 100 (+50)` | `100 (+50) 物理伤害` |
| `default` | `物理伤害: 10 (+20)%` | `10 (+20)% 物理伤害` |
| `default` | `物理伤害*2`        | `2* 物理伤害`        |
| `percent` | `暴击几率: 25%`     | `25% 暴击几率`       |
| `percent` | `暴击几率: 10-20%`  | `10-20% 暴击几率`    |
| `percent` | `暴击几率: 50 (+10)%` | `50 (+10)% 暴击几率` |
| `roman` | `力量: V`         | `V 力量`           |

::: tip 两种百分比写法
`default` 会把 `附加伤害: 50%` 读成 `percent = 50`，用作百分比加成基础值；  
而 `percent` 是把 `暴击几率: 25%` 直接读成 `value = 25`，后面的 `%` 只是个装饰，不会作为 `percent` 去计算百分比加成。  
所以习惯用百分比描述的属性（比如暴击几率、闪避几率、吸血几率）应该使用 `percent` 读取组。
:::

::: info default 的一些奇妙小写法
- 百分比范围：`物理伤害: 10-20%`（`percentMin/percentMax`）。
- 括号追加值：`物理伤害: 100 (+50)`（`value + valueAddition`）。
- 括号追加百分比：`物理伤害: 10 (+20)%`（`percent + percentAddition`）。
- 乘算：`物理伤害*2`（`mul`）。
:::

## 读取组芝士之捕获组 (`captures`)：保存读到的值

`captures` 声明了需要提取哪些值，以及当多件装备（或多行 Lore）都提供了这个值时怎么合并。

```yaml :line-numbers
default:
  // [!code focus:10]
  captures:
    value: add
    valueAddition: add
    min: add
    max: add
    percent: add
    percentAddition: add
    percentMin: add
    percentMax: add
    mul: multiply
  patterns:
    // 略
  placeholders:
    // 略
```

`captures` 下的节点对应捕获组的名称，节点的值对应捕获组要捕获的东西。

::: code-group
```yaml [简写形式] :line-numbers
# 声明一个叫做 value 的捕获组：
# 仅指定合并方式为 add
# 默认读取格式为 decimal
value: add
```
```yaml [完整格式] :line-numbers
# 明确指定合并方式与读取格式
# 这个例子就是简写形式对应的完整格式
value:
  op: add
  format: decimal
```
:::

<ParamList
  title="读取格式"
  :items="[
    { name: 'decimal', default: '0.0', type: 'Double', desc: '十进制正负整数或小数（匹配正则 <code>[-+]?\\d+(?:\\.\\d+)?</code>）', example: '攻击力: 100  或  攻击力: -12.5' },
    { name: 'roman', default: '0.0', type: 'Double', desc: '罗马数字，解析后会换算成对应的数字（见 <code>config.yml</code> 的 <code>formats.roman</code>）', example: '力量: IV (换算为 4.0)' },
    { name: 'text', default: '空文本', type: 'String', desc: '任意非空字符串（匹配正则 <code>.+</code>），一般用于权限、职业、槽位等文本条件', example: '职业: 战士/法师' }
  ]"
/>

<ParamList
  title="合并方式"
  :items="[
    { name: 'add', type: '数值', desc: 'a 与 b 相加（比如两件装备各提供 100，合并为 200）', example: '1 + 2 = 3' },
    { name: 'multiply', type: '数值', desc: 'a 与 b 相乘', example: '1 * 2 = 2' },
    { name: 'max / min', type: '数值', desc: '只保留所有来源中最大（<code>max</code>）或最小（<code>min</code>）的那个值', example: '1 和 2 只留 2(max) 或 1(min)' },
    { name: 'last', type: '文本', desc: 'b 覆盖 a（按属性源 <code>priority</code> 从小到大顺序）', example: '张三 和 李四 只留李四' },
    { name: 'skip', type: '文本', desc: '只留 a', example: '张三 和 李四 只留张三' },
    { name: 'append', type: '文本', desc: 'b 追加在 a 后面', example: '张三 和 李四 变成张三李四' }
  ]"
/>

## 读取组芝士之匹配规则 (`patterns`)

`patterns` 是一个正则表达式列表，支持两个内部占位符：
- `{name}`：代表属性、条件的识别名 `names`。
- `<capture>`：代表捕获组 `captures`

::: danger 规则顺序问题
在读取组尝试读取时，会按照**从上到下**的顺序挨个测试匹配规则，一旦有符合规则的结果，就不会判断后续规则了。

也就是说，如果把宽松的规则放在严格规则上面，~~宽松规则会直接夺走严格规则的人生~~。

所以实际写配置的时候，应该把更严格的规则放在上面，把更宽松的规则放在下面。
:::

下面的框框里可以测试 `value` 放在上面的后果：

<ReaderDemo
  :rules="rules"
  :normal-order="['range', 'percent', 'value']"
  :alternate-order="['value', 'range', 'percent']"
  toggle-label="把 <code>value</code> 规则放到最上面"
  attr-name="附加伤害"
  :initial-lines="['附加伤害: 10', '附加伤害: 50%', '附加伤害: 5-15']"
/>

## 读取组芝士之预计算 (`placeholders`)

`placeholders` 就是基于 `captures` 提取出来的值设定计算公式，方便属性逻辑、papi 变量直接取得结果，很好理解，没什么好说的。