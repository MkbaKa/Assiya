# 伤害快照与派发 Snapshot

在做多段连斩或延迟引爆技能时，如果希望**在技能起手那一刻锁定攻击面板**、后续每一段都复用这份面板结算，可以配合使用 `assiya:ds`（存档）与 `assiya:dsd`（派发伤害）。

<FlowSteps
  :columns="2"
  :steps="[
    { tag: '第 1 步', title: '记录快照 (assiya:ds)', sub: '只存档，不扣血', desc: '锁定属性面板、临时词条与黑白名单，按指定的编号 <code>id</code> 存入目标会话中。' },
    { tag: '第 2 步', title: '派发伤害 (assiya:dsd)', sub: 'fork 副本并扣血', desc: '凭编号 <code>id</code> 取出快照，复制出针对当前目标的独立处理器，结合受击方实时防御完成扣血。' }
  ]"
/>

## 1. 记录快照：`assiya:damage-snapshot`

<SyntaxBar
  label="技能示例"
  syntax="- assiya:ds{i=快照ID;攻击力=120;...} @target"
  :aliases="['assiya:damage-snapshot', 'assiya:damagesnapshot', 'assiya:ds']"
/>

<ParamList
  title="damage-snapshot 参数（其余键值对均作为临时属性或 CustomData 存入快照）"
  :items="[
    { name: 'id', aliases: ['i'], required: true, type: 'String', desc: '快照唯一标识 ID，后续派发时凭此 ID 读取。' },
    { name: 'clear', aliases: ['c', 'isClear'], default: 'false', type: 'Boolean', desc: '是否清空原面板属性。' },
    { name: 'overwrite', aliases: ['ow'], default: 'false', type: 'Boolean', desc: '技能行属性是否覆盖原同名属性。' },
    { name: 'usableAttribute / disableAttribute', aliases: ['ua', 'da'], default: '无', type: 'String', desc: '允许或禁止参与结算的属性白名单 / 黑名单，用英文逗号 <code>,</code> 分隔。' }
  ]"
/>

## 2. 派发伤害：`assiya:damage-snapshot-dispatch`

记录快照时，临时属性与属性列表沿用 [属性伤害](./damage-meta.md) 的写法，显式捕获组和列表条目均支持属性 id 或读取名。

<SyntaxBar
  label="技能示例"
  syntax="- assiya:dsd{i=快照ID;im=true;pkb=true;ndt=0} @target"
  :aliases="['assiya:damage-snapshot-dispatch', 'assiya:damagesnapshotdispatch', 'assiya:dsd']"
/>

<ParamList
  title="damage-snapshot-dispatch 参数"
  :items="[
    { name: 'id', aliases: ['i'], required: true, type: 'String', desc: '要读取的快照 ID（注意：<code>ds</code> 存在哪个目标身上，<code>dsd</code> 就会从哪个目标会话上读取）。' },
    { name: 'ignoreImmunity / preventKnockback / noDamageTicks', aliases: ['im', 'pkb', 'ndt'], default: 'false / false / 20', type: '控制项', desc: '与 <code>assiya:dm</code> 完全一致，控制无敌帧与原版击退。' },
    { name: 'signal / postAttackSkill', aliases: ['s', 'pas'], default: '无', type: '联动项', desc: '扣血前发送的 MM 信号名，以及扣血后触发的子技能名。' }
  ]"
/>

## 完整配合示例

```yaml [三段印记引爆技能] :line-numbers
MarkDetonate:
  Skills:
    # 1. 在目标身上存下编号为 mark_1 的伤害快照
    - assiya:ds{i=mark_1;攻击力=120;da=反弹几率} @target
    # 2. 延迟 10 tick 派发第一段伤害（无击退，不留无敌帧）
    - delay 10
    - assiya:dsd{i=mark_1;im=true;pkb=true;ndt=0} @target
    # 3. 再延迟 10 tick 派发第二段伤害（恢复 10 tick 无敌帧）
    - delay 10
    - assiya:dsd{i=mark_1;im=true;pkb=false;ndt=10} @target
```
