# 属性源检测 HasAttributeSource

<SyntaxBar
  label="条件示例"
  syntax="- assiya:has-attr-source{s=来源名称}"
  :aliases="['assiya:has-attr-source', 'assiya:hasattrsource']"
  badge="实体条件"
/>

判断目标是否拥有特定属性源。

<ParamList
  title="条件参数"
  :items="[
    { name: 'source', aliases: ['s'], required: true, type: 'String', desc: '要检查的属性源名称', example: 's=boss_rage' }
  ]"
/>

## 示例配置

```yaml
Conditions:
  - assiya:has-attr-source{s=boss_rage} true
```
