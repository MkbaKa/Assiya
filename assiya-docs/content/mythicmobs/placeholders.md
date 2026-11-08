# MythicMobs 占位符

::: danger 注意
本页列出的占位符均由 **MythicMobs** 解析，写作 `<xxx>` 格式，无法在 PlaceholderAPI 中写作 `%xxx%`
:::

以下占位符支持 `caster`、`trigger`、`target`、`parent`、`owner` 五种读取对象（下方统一以 `caster` 为例）：

属性位置支持 id 或读取名，例如 `<caster.attr.物理伤害:total:0>`。优先匹配 id（不区分大小写），未命中时精确匹配读取名。

<ParamList
  title="MythicMobs 可用占位符"
  :items="[
    { name: '<caster.attr.属性ID[:捕获名][:小数位数]>', default: 'total / 2位小数', type: '属性数值', desc: '读取对象的指定属性值。省略后两项时默认读取 <code>total</code> 并保留 2 位小数。', example: '<caster.attr.physical-attack:total:0>' },
    { name: '<caster.damagedata.键名[|默认值]>', default: 'null', type: '伤害标记', desc: '读取当前正在执行的伤害处理器 <code>CustomDataComponent</code> 中的值。', example: '<caster.damagedata.crit-damage|0>' },
    { name: '<caster.metadata.键名[|默认值]>', default: 'null', type: '会话数据', desc: '读取实体 <code>AssiyaSession.metadata</code> 中的值（如弓箭的蓄力力度 <code>Assiya:force</code>）。', example: '<caster.metadata.Assiya:force|1.0>' },
    { name: '<owner.uuid> / <owner.name>', default: 'null', type: '召唤物主人', desc: '获取当前召唤物主人的 UUID 或名称。' }
  ]"
/>

## 示例配置

```yaml
Skills:
  - m{m="当前物理伤害： <caster.attr.physical-attack:total:0>"} @self
  - m{m="目标的护甲： <target.attr.armor:total:1>"} @self
  - m{m="本次暴击额外伤害： <caster.damagedata.crit-damage|0>"} @self
```
