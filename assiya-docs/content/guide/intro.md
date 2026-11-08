# 这是什么？

Assiya 是一款服务于 RPG 玩法的属性系统。

提供了高度客制化的属性、条件、读取规则与执行逻辑等内容。

可以真正意义上的定义一个属性创造后的行为。

并提供了独立于 Bukkit API 的伤害过程处理，以实现各种奇思妙想。

~~可能需要一定兼容生态，目前该部分内容仅深度兼容了 MythicMobs~~

## 前置与可选依赖

<ParamList
  title="前置插件"
  :items="[
    { name: 'PlaceholderAPI', required: '必备', type: 'depend', desc: '提供占位符读取' },
    { name: 'MythicMobs', default: '可选', type: 'v4 / v5', desc: '怪物属性与技能、条件' },
    { name: 'NeigeItems', default: '可选', type: 'NBT 读取', desc: 'NBT 读取' }
  ]"
/>

## 目录结构

```text
Assiya/
├── config.yml          # 全局设置
├── attribute/          # 属性配置
├── condition/          # 条件配置
├── formula/            # 公式配置
├── lang/               # 语言文件
├── reader/             # 读取组配置
└── scripts/            # 自定义脚本
```