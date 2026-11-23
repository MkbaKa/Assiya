# 默认属性与条件

*默认属性大多使用 `default` 读取组；「几率」类的触发属性使用 `percent` 读取组。*

## 默认属性

可以通过标签快速选择属性，如果选择了好几个标签，会显示同时符合条件的属性。
<AttrCatalog
  placeholder="搜索属性 ID 或读取名（如 crit / 暴击 / 护甲）..."
  :tag-order="[
    '攻击触发', '受击触发', '更新触发', '定时触发',
    '物理', 'PVP', 'PVE', '真实伤害',
    '护甲', '暴击', '吸血', '闪避', '远程', '反弹',
    '雷击', '燃烧', '冰冻', '生命', '移动'
  ]"
  :items="[
    { id: 'physical-attack', names: ['物理伤害', '攻击力'], tags: ['物理', '攻击触发'], priority: 100, file: 'fight/attack.yml', desc: '增加伤害' },
    { id: 'pvp-attack', names: ['PVP伤害'], tags: ['PVP', '攻击触发'], priority: 110, file: 'fight/attack.yml', desc: '攻击玩家时增加伤害' },
    { id: 'pve-attack', names: ['PVE伤害'], tags: ['PVE', '攻击触发'], priority: 120, file: 'fight/attack.yml', desc: '攻击非玩家时增加伤害' },
    { id: 'lightning', names: ['雷击几率'], tags: ['雷击', '攻击触发'], priority: 150, file: 'fight/lightning.yml', desc: '攻击时有几率召唤闪电特效，并造成雷击伤害' },
    { id: 'vampire', names: ['吸血几率'], tags: ['吸血', '攻击触发'], priority: 160, file: 'fight/vampire.yml', desc: '攻击时有概率触发吸血' },
    { id: 'crit', names: ['暴击几率'], tags: ['暴击', '攻击触发'], priority: 300, file: 'fight/crit.yml', desc: '攻击时有概率触发暴击' },
    { id: 'fire', names: ['燃烧几率'], tags: ['燃烧', '攻击触发'], priority: 320, file: 'misc.yml', desc: '攻击时有几率点燃目标，并造成燃烧伤害（点燃只是个特效）' },
    { id: 'frozen', names: ['冰冻几率'], tags: ['冰冻', '攻击触发'], priority: 330, file: 'misc.yml', desc: '攻击时有几率降低目标的移动速度' },
    { id: 'real-attack', names: ['真实伤害'], tags: ['真实伤害', '攻击触发'], priority: 340, file: 'fight/attack.yml', desc: '不被防御减免的伤害值' },
    { id: 'dodge', names: ['闪避几率'], tags: ['闪避', '受击触发'], priority: 0, file: 'fight/dodge.yml', desc: '受到攻击有概率触发闪避，免疫此次伤害' },
    { id: 'remote-immune', names: ['远程免疫', '箭伤免疫率'], tags: ['闪避', '远程', '受击触发'], priority: 10, file: 'misc.yml', desc: '只针对远程伤害的闪避几率' },
    { id: 'physical-defense', names: ['物理防御', '防御力'], tags: ['物理', '受击触发'], priority: 200, file: 'fight/attack.yml', desc: '减免物理伤害，如果伤害小于防御值，那么伤害会变成 0 而不是负数' },
    { id: 'pvp-defense', names: ['PVP防御'], tags: ['PVP', '受击触发'], priority: 210, file: 'fight/attack.yml', desc: '抵消 PVP 伤害' },
    { id: 'pve-defense', names: ['PVE防御'], tags: ['PVE', '受击触发'], priority: 220, file: 'fight/attack.yml', desc: '抵消 PVE 伤害' },
    { id: 'armor', names: ['护甲', '护甲值'], tags: ['护甲', '受击触发'], priority: 250, file: 'fight/armor.yml', desc: '受到攻击时按 <code>armor</code> 公式计算减伤值' },
    { id: 'reflection', names: ['反弹几率'], tags: ['反弹', '受击触发'], priority: 310, file: 'misc.yml', desc: '受到攻击时有几率反弹伤害' },
    { id: 'max-health', names: ['生命上限', '生命力'], tags: ['生命', '更新触发'], priority: 100, file: 'misc.yml', desc: '增加生命上限' },
    { id: 'moving', names: ['移动速度', '移速加成'], tags: ['移动', '更新触发'], priority: 110, file: 'misc.yml', desc: '按百分比增加移动速度，<code>移动速度: 10</code> 表示快 10%' },
    { id: 'health-regain', names: ['生命恢复'], tags: ['生命', '定时触发'], priority: 100, file: 'misc.yml', desc: '每秒恢复生命' },
    { id: 'hit', names: ['命中几率'], tags: ['闪避'], priority: null, file: 'fight/dodge.yml', desc: '降低对方的闪避几率' },
    { id: 'sunder-armor', names: ['破甲几率'], tags: ['护甲'], priority: null, file: 'fight/armor.yml', desc: '攻击时有几率让对方的护甲不参与计算' },
    { id: 'see-through', names: ['护甲穿透'], tags: ['护甲'], priority: null, file: 'fight/armor.yml', desc: '降低对方护甲在公式中的数值' },
    { id: 'crit-rate', names: ['暴伤倍率'], tags: ['暴击'], priority: null, file: 'fight/crit.yml', desc: '触发暴击后百分比增加伤害' },
    { id: 'crit-resist', names: ['暴击抵抗'], tags: ['暴击'], priority: null, file: 'fight/crit.yml', desc: '抵消暴击几率' },
    { id: 'crit-dodge', names: ['暴击闪避'], tags: ['暴击'], priority: null, file: 'fight/crit.yml', desc: '对方暴击时，有几率让这次暴击无效' },
    { id: 'crit-damage-resist', names: ['暴伤抵抗'], tags: ['暴击'], priority: null, file: 'fight/crit.yml', desc: '抵消暴伤倍率' },
    { id: 'lightning-damage', names: ['雷击伤害'], tags: ['雷击'], priority: null, file: 'fight/lightning.yml', desc: '雷击造成的额外伤害' },
    { id: 'vampire-rate', names: ['吸血倍率'], tags: ['吸血'], priority: null, file: 'fight/vampire.yml', desc: '触发吸血后，按此次伤害的百分比回复血量' },
    { id: 'vampire-resist', names: ['吸血抵抗'], tags: ['吸血'], priority: null, file: 'fight/vampire.yml', desc: '抵消吸血几率' },
    { id: 'vampire-dodge', names: ['吸血闪避'], tags: ['吸血'], priority: null, file: 'fight/vampire.yml', desc: '对方吸血时，有几率让这次吸血无效' },
    { id: 'reflection-rate', names: ['反弹倍率'], tags: ['反弹'], priority: null, file: 'misc.yml', desc: '反弹当前总伤害的百分之多少' },
    { id: 'fire-damage', names: ['燃烧伤害'], tags: ['燃烧'], priority: null, file: 'misc.yml', desc: '点燃时造成的额外伤害' },
    { id: 'fire-time', names: ['燃烧时间'], tags: ['燃烧'], priority: null, file: 'misc.yml', desc: '在基础 3 秒上增加的燃烧时间' },
    { id: 'frozen-intensity', names: ['冰冻强度'], tags: ['冰冻'], priority: null, file: 'misc.yml', desc: '降低移动速度的百分比' },
    { id: 'frozen-time', names: ['冰冻时间'], tags: ['冰冻'], priority: null, file: 'misc.yml', desc: '在基础 3 秒上增加的减速时间' }
  ]"
/>

## 默认条件

<SyntaxBar
  label="配置文件"
  syntax="condition/*.yml"
  path="plugins/Assiya/condition/example.yml"
/>

<ConditionCatalog
  :items="[
    {
      id: 'level-require', name: '等级需求', names: ['等级需求', 'Lv.'], reader: 'default',
      desc: '检查玩家的原版经验等级，可以设置最低等级或等级区间。',
      examples: [
        { code: '等级需求: 10', description: '至少 10 级' },
        { code: '等级需求: 10-20', description: '10～20 级' }
      ],
      note: '非玩家实体默认通过。'
    },
    {
      id: 'permission', name: '权限需求', names: ['权限需求', '职业'], reader: 'text',
      desc: '检查玩家是否拥有指定权限节点。多个权限用斜杠 / 分隔，拥有其中任意一个即可通过。',
      examples: [
        { code: '职业: 战士/法师/弓箭手', description: '拥有任意一个权限即可' }
      ]
    },
    {
      id: 'equipment-slot', name: '装备槽位', names: ['装备槽位', '装备类型'], reader: 'text',
      desc: '限制物品所在的装备槽位。多个槽位用斜杠 / 分隔，在其中任意一个槽位即可通过。',
      examples: [
        { code: '装备槽位: 主手/副手', description: '主手或副手' },
        { code: '装备槽位: 双持', description: '与上一条写法相同' }
      ],
      values: ['主手', '副手', '双持', '头盔', '衣服', '裤子', '鞋子'],
      note: '双持表示主手或副手。'
    }
  ]"
/>
