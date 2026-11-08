# 属性值检测 AttributeValue

<SyntaxBar
  label="条件示例"
  syntax="- assiya:attribute-value{attr=要判断的属性;c=捕获名;v=比较式}"
  :aliases="['assiya:attribute-value', 'assiya:attributevalue']"
  badge="实体条件"
/>

判断目标的属性值是否符合需求。

<ParamList
  title="条件参数"
  :items="[
    { name: 'name', aliases: ['n', 'attribute', 'attr'], required: true, type: 'String', desc: '要检查的属性名' },
    { name: 'capture', aliases: ['c'], default: 'total', type: 'String', desc: '要读取的 capture 或 placeholder 名称' },
    { name: 'action', aliases: ['a', 'value', 'v'], default: '>0', type: 'JS 比较式', desc: '由 js 执行的比较规则', example: 'v=>=50  或  v=<20' }
  ]"
/>

## 示例配置

```yaml
Conditions:
  # 暴击几率大于等于 50
  - assiya:attribute-value{attr=暴击几率;v=>=50} true
  # 护甲低于 20
  - assiya:attribute-value{attr=armor;c=total;v=<20} true
```
