# 伤害数据检测 DamageData

<SyntaxBar
  label="条件示例"
  syntax="- assiya:check-data{k=数据名;v=值;us=false}"
  :aliases="['assiya:check-damage-data', 'assiya:check-data', 'assiya:checkdamagedata', 'assiya:checkdata']"
  badge="实体条件"
/>

在伤害触发技能（如 `~onDamaged` 或 `postAttackSkill`）中，检查当前伤害过程 `CustomDataComponent` 里记录的数据。

<ParamList
  title="条件参数"
  :items="[
    { name: 'key', aliases: ['k'], required: true, type: 'String', desc: '属性脚本或技能行写入 <code>CustomDataComponent</code> 的数据名（比如 <code>is-crit</code>、<code>crit-damage</code>）' },
    { name: 'value', aliases: ['v'], required: true, type: 'String', desc: '期望匹配的文本，或一段比较表达式' },
    { name: 'useScript', aliases: ['script', 'us'], default: 'false', type: 'Boolean', desc: '为 <code>false</code> 时直接判断字符串相等；为 <code>true</code> 时将 <code>数据值 + value</code> 作为 JS 表达式求布尔值' }
  ]"
/>

## 示例配置

```yaml
Conditions:
  # 判断本次攻击是否触发了暴击
  - assiya:check-data{k=is-crit;v=true}

  # 开启 useScript 判断本次暴击额外伤害是否大于 100
  - assiya:check-data{k=crit-damage;v=>100;us=true}
```

## 与 damage-meta 组合

[damage-meta](../mechanic/damage-meta.md) 技能行中，除固定参数外的额外参数都会记录到本次伤害的数据中。可以给不同技能写入不同的标记，再用 `check-data` 判断这次伤害来自哪个技能。

比如，通过技能指定 `attack-kind=heavy`。目标受到这次伤害时，通过 `~onDamaged` 调用响应技能，判断这次攻击是否为重击。

```yaml :line-numbers
HeavyStrike:
  Skills:
    - assiya:dm{attack-kind=heavy;物理伤害=150} @target

OnHeavyStrike:
  TriggerConditions:
    - assiya:check-data{k=attack-kind;v=heavy} true
  Skills:
    - message{m="受到了重击"} @PlayersInRadius{r=10}
```

记得在怪物配置里加上：

```yaml
TrainingZombie:
  Type: ZOMBIE
  // [!code focus:2]
  Skills:
    - skill{s=OnHeavyStrike} @self ~onDamaged
```

或者判断这次技能的属性参数？

```yaml
SkillDamageCheck:
  TriggerConditions:
    - assiya:check-data{k=物理伤害;v=>150;useScript} true
  Skills:
    - message{m="damage-meta 技能里的物理伤害大于 150 点!"} @PlayersInRadius{r=10}
```

## 默认属性会记录的数据

可以组合的对象不仅仅是 damage-meta，下面列了默认属性会保存的数据，它们也能提供很多不错的灵感。如果你想存什么东西也可以去对应脚本里加。

<ParamList
  title="内置脚本自动写入 CustomDataComponent 的键名"
  :items="[
    { name: 'is-dodge', aliases: ['dodge 闪避'], type: 'Boolean', desc: '触发闪避时写入 <code>true</code>' },
    { name: 'is-remote-immune', aliases: ['remote-immune 远程免疫'], type: 'Boolean', desc: '触发远程免疫时写入 <code>true</code>' },
    { name: 'is-sunder-armor / armor-reduction', aliases: ['armor 护甲'], type: 'Boolean / Double', desc: '触发破甲时写入 <code>is-sunder-armor = true</code>；未破甲时写入 <code>armor-reduction</code>（减免伤害值）' },
    { name: 'is-crit / crit-damage', aliases: ['crit 暴击'], type: 'Boolean / Double', desc: '触发暴击时写入 <code>is-crit = true</code> 与 <code>crit-damage</code>（暴击额外伤害值）' },
    { name: 'is-lightning / lightning-damage', aliases: ['lightning 雷击'], type: 'Boolean / Double', desc: '触发雷击时写入 <code>is-lightning = true</code> 与 <code>lightning-damage</code>' },
    { name: 'is-vampire / vampire-heal / vampire-extra', aliases: ['vampire 吸血'], type: 'Boolean / Double', desc: '触发吸血时写入是否触发、回血量以及是否开启额外伤害' },
    { name: 'is-reflection / reflection-damage', aliases: ['reflection 反弹'], type: 'Boolean / Double', desc: '触发反弹时写入 <code>is-reflection = true</code> 与反弹伤害值' },
    { name: 'is-fire / fire-damage / fire-ticks', aliases: ['fire 燃烧'], type: 'Boolean / Double / Int', desc: '触发燃烧时写入附加伤害与燃烧时长 (tick)' },
    { name: 'is-frozen / frozen-rate / frozen-duration', aliases: ['frozen 冰冻'], type: 'Boolean / Double', desc: '触发冰冻时写入减速比例与持续秒数' }
  ]"
/>
