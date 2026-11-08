# 继承属性 ExtendAttribute

<SyntaxBar
  label="技能示例"
  syntax="- assiya:extend-attr{reverse=false;d=-1} @target"
  :aliases="['assiya:extend-attr', 'assiya:extendattr']"
  badge="实体目标技能"
/>

把实体当前拥有的属性复制给另外一个实体，比如召唤物继承属性、分身复制属性之类的。

<ParamList
  title="技能参数"
  :items="[
    { name: 'reverse', default: 'false', type: 'Boolean', desc: '控制属性复制方向：<br>• 为 <code>false</code> 时：<b>施法者 (caster) 继承目标 (target) 的属性</b><br>• 为 <code>true</code> 时：<b>目标 (target) 继承施法者 (caster) 的属性</b>' },
    { name: 'duration', aliases: ['d'], default: '-1', type: 'Long (tick)', desc: '继承过来的属性源持续时间（tick），填 <code>-1</code> 持续到死亡/下线' }
  ]"
/>

## 例子

::: code-group
```yaml [召唤物出生时继承主人 (reverse=false)]
# 写在召唤物自己的配置里：出生时对 @owner 释放，把主人的属性复制到自己身上
shadow_clone:
  Type: ZOMBIE
  Skills:
    - assiya:extendattr @owner ~onSpawn
```
```yaml [主人把自身属性传给目标 (reverse=true)]
# 写在主人的技能里：把施法者自己的属性复制给 @target，持续 200 tick
Skills:
  - assiya:extendattr{reverse=true;d=200} @target
```
:::
