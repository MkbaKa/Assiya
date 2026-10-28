import {defineConfig} from 'vitepress'

export default defineConfig({
  title: 'Assiya',
  description: 'Assiya 文档',
  lang: 'zh-CN',
  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: '文档', link: '/content/getting-start/info' }
    ],
    sidebar: [
      {
        text: '简介',
        items: [
          { text: '什么是 Assiya?', link: '/content/getting-start/info' },
          { text: '快速开始', link: '/content/getting-start/start' },
          { text: '文件结构', link: '/content/getting-start/file-structure' },
          { text: '为物品增加属性', link: '/content/getting-start/item-attribute' },
        ]
      },
      {
        text: '配置',
        items: [
          { text: 'config.yml', link: '/content/configuration/global' },
        ]
      },
      {
        text: '进阶知识',
        items: [
          { text: 'NBT 结构', link: '/content/advanced/nbt-structure' }
        ]
      },
    ],
    search: {
      provider: 'local'
    },
    outline: {
      label: '本页目录'
    },
    returnToTopLabel: '返回顶部',
    sidebarMenuLabel: '菜单',
    darkModeSwitchLabel: '外观',
    lightModeSwitchTitle: '切换至浅色模式',
    darkModeSwitchTitle: '切换至深色模式'
  }
})

