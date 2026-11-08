# 属性伤害 DamageMeta

<SyntaxBar
  label="技能示例"
  syntax="- assiya:dm{im=true;pkb=true;属性项...} @target"
  :aliases="['assiya:damage-meta', 'assiya:damagemeta', 'assiya:dm']"
  badge="实体目标技能"
/>

对技能目标发起一次经 Assiya 属性结算的伤害。支持在本次伤害中临时增加或覆盖攻击方属性、设置参与结算的属性黑白名单，以及控制无敌帧和击退。

<MmLineBuilder
  :targeters="['@target', '@self', '@EIR{r=5}', '@PIR{r=10}', '@trigger', '@owner']"
  title="属性伤害"
  skill="assiya:damagemeta"
  targeter="@target"
  :fields="[
    { id: 'attrs', label: '附加属性词条', default: '攻击力=150;暴击几率=30', raw: true, wide: true },
    { id: 'ua', key: 'usableAttribute', label: '属性白名单 ', default: '', placeholder: '留空不限，如 物理伤害,护甲' },
    { id: 'da', key: 'disableAttribute', label: '禁用属性黑名单', default: '闪避几率', placeholder: '如 闪避几率,反弹几率' },
    { id: 'ndt', key: 'noDamageTicks', label: '受击后无敌帧', type: 'number', default: 0, min: 0, max: 100, omitIf: 20 }
  ]"
  :switches="[
    { id: 'im', key: 'ignoreImmunity', label: '无视当前无敌帧', default: true },
    { id: 'pkb', key: 'preventKnockback', label: '取消原版击退', default: true },
    { id: 'c', key: 'clear', label: '不计算施法者本身的属性', default: false },
    { id: 'ow', key: 'overwrite', label: '覆盖同名属性 ', default: false }
  ]"
/>

## 参数列表

显式属性词条支持 `属性 id 或读取名|捕获组名=值`，例如 `物理伤害|min=100;physical-attack|max=200`。属性列表 `usableAttribute`、`disableAttribute` 也支持 id 与读取名混用。查询时 id 优先且不区分大小写，读取名精确匹配。

除了以下固定参数外，花括号内填写的**其余参数**：
- 若节点名能匹配到属性（如 `攻击力=150` 或 `physical-attack|min=100`），会作为**本次伤害的临时攻击属性**参与结算；
- 同时所有额外参数都会存入本次处理器的 `CustomDataComponent` 中，可在脚本或 [DamageData 条件](../condition/cond-damage-data.md) 中读取。

<ParamList
  title="属性相关参数"
  :items="[
    { name: 'attacker', aliases: ['player', 'from', 'source'], default: 'caster', type: 'String', desc: '指定本次伤害的攻击者（名称或 UUID），未找到时默认取当前施法者' },
    { name: 'clear', aliases: ['c', 'isClear'], default: 'false', type: 'Boolean', desc: '设为 <code>true</code> 时清空攻击者原有的属性，只使用本技能行里写的属性进行结算' },
    { name: 'overwrite', aliases: ['ow'], default: 'false', type: 'Boolean', desc: '为 <code>false</code> 时技能行属性与原属性合并累加；为 <code>true</code> 时直接覆盖原来同名属性的属性值' },
    { name: 'usableAttribute', aliases: ['ua'], default: '不限制', type: 'String', desc: '这次伤害能让哪些属性执行，多个用英文逗号 <code>,</code> 分隔（未列出的 attack、defense 属性不会执行）', example: 'usableAttribute=物理伤害,护甲' },
    { name: 'disableAttribute', aliases: ['da'], default: '无', type: 'String', desc: '本次计算禁用的属性，多个用英文逗号 <code>,</code> 分隔', example: 'disableAttribute=闪避几率,反弹几率' }
  ]"
/>

<ParamList
  title="伤害控制参数"
  :items="[
    { name: 'ignoreImmunity', aliases: ['im', 'pi', 'ii'], default: 'false', type: 'Boolean', desc: '是否无视受击方当前的原版无敌帧' },
    { name: 'preventKnockback', aliases: ['pkb', 'pk'], default: 'false', type: 'Boolean', desc: '是否取消本次伤害产生的原版击退' },
    { name: 'noDamageTicks', aliases: ['ndt'], default: '20', type: 'Int (tick)', desc: '伤害结算完成后给受击方设置的无敌帧时长。做多段连击应该写 <code>ndt=0</code>' },
    { name: 'signal', aliases: ['s'], default: '无', type: 'String', desc: '在伤害算完、扣血前向目标发送的 MM 信号名，并附带变量 <code>&amp;lt;skill.var.damage-amount&amp;gt;</code>' },
    { name: 'postAttackSkill', aliases: ['postAttack', 'pas'], default: '无', type: 'String', desc: '执行伤害后执行的 MM 技能名' }
  ]"
/>

## 例子

::: code-group
```yaml [常规強化攻击]
Skills:
  # 基于自身面板造成伤害，本次额外增加 150 物理伤害与 30 暴击几率，无视无敌帧、无击退，且禁用目标闪避
  - assiya:dm{ignoreImmunity=true;preventKnockback=true;disableAttribute=闪避几率;物理伤害=150;暴击几率=30} @target
```
```yaml [独立面板与白名单结算]
Skills:
  # 清空自身面板 (clear=true)，只造成 100~200 物理伤害，且只允许物理伤害与护甲参与计算
  - assiya:dm{clear=true;usableAttribute=物理伤害,护甲;物理伤害|min=100;物理伤害|max=200} @EIR{r=5}
```
:::
