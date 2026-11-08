# 移除属性 TakeAttribute

<SyntaxBar
  label="技能示例"
  syntax="- assiya:take-attr{s=来源名称} @target"
  :aliases="['assiya:take-attr', 'assiya:takeattr']"
  badge="实体目标技能"
/>

移除技能目标身上的指定属性源。

<ParamList
  title="技能参数"
  :items="[
    { name: 'source', aliases: ['s'], required: true, type: 'String', desc: '要移除的属性源名称（与 <code>add-attr</code> 时指定的 <code>s</code> 对应）', example: 's=boss_rage' }
  ]"
/>

## 例子

```yaml
Skills:
  - assiya:takeattr{s=boss_rage} @self
```
