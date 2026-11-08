import { defineConfig } from 'vitepress'

// 把 ```mermaid 代码块交给 Mermaid 组件在浏览器中渲染
function mermaidFence(md) {
  const fence = md.renderer.rules.fence
  md.renderer.rules.fence = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    if (token.info.trim() === 'mermaid') {
      return `<Mermaid code="${md.utils.escapeHtml(token.content)}" />`
    }
    return fence(tokens, idx, options, env, self)
  }
}

export default defineConfig({
  title: 'Assiya',
  description: 'Assiya 文档',
  lang: 'zh-CN',
  srcExclude: ['backup/**'],
  markdown: {
    config: mermaidFence
  },
  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: '开始', link: '/content/guide/intro' },
      { text: '配置指南', link: '/content/config/defaults' },
      { text: 'MythicMobs 拓展', link: '/content/mythicmobs/mob-attributes' },
      { text: '脚本与开发', link: '/content/advanced/damage-flow' }
    ],
    sidebar: [
      {
        text: '快速开始',
        items: [
          { text: '这是什么？', link: '/content/guide/intro' },
          { text: '为物品添加属性', link: '/content/guide/quick-start' },
          { text: '命令', link: '/content/guide/commands' },
          { text: 'PlaceholderAPI 变量', link: '/content/guide/placeholders' }
        ]
      },
      {
        text: '配置指南',
        items: [
          { text: '默认属性与可用条件', link: '/content/config/defaults' },
          { text: '属性配置', link: '/content/config/attributes' },
          { text: '读取组与自定义格式', link: '/content/config/readers' },
          { text: '使用条件与生效范围', link: '/content/config/conditions' },
          { text: '调整计算公式', link: '/content/config/formulas' },
          { text: '在 NBT 中编写属性', link: '/content/config/nbt' }
        ]
      },
      {
        text: 'MythicMobs 拓展',
        items: [
          { text: '怪物属性与等级公式', link: '/content/mythicmobs/mob-attributes' },
          {
            text: '技能',
            collapsed: false,
            items: [
              { text: '给予属性 · AddAttribute', link: '/content/mythicmobs/mechanic/add-attr' },
              { text: '移除属性 · TakeAttribute', link: '/content/mythicmobs/mechanic/take-attr' },
              { text: '继承属性 · ExtendAttribute', link: '/content/mythicmobs/mechanic/extend-attr' },
              { text: '属性伤害 · DamageMeta', link: '/content/mythicmobs/mechanic/damage-meta' },
              { text: '伤害快照 · Snapshot', link: '/content/mythicmobs/mechanic/damage-snapshot' }
            ]
          },
          {
            text: '条件',
            collapsed: false,
            items: [
              { text: '属性值 · AttributeValue', link: '/content/mythicmobs/condition/cond-attribute-value' },
              { text: '属性源 · HasAttributeSource', link: '/content/mythicmobs/condition/cond-has-source' },
              { text: '伤害数据 · DamageData', link: '/content/mythicmobs/condition/cond-damage-data' }
            ]
          },
          { text: '占位符', link: '/content/mythicmobs/placeholders' }
        ]
      }
    ],
    search: {
      provider: 'local'
    },
    outline: {
      level: [2, 3],
      label: '本页目录'
    },
    returnToTopLabel: '返回顶部',
    sidebarMenuLabel: '菜单',
    darkModeSwitchLabel: '外观',
    lightModeSwitchTitle: '切换至浅色模式',
    darkModeSwitchTitle: '切换至深色模式'
  }
})
