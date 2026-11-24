<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset=".github/assets/readme/hero-dark.webp">
    <img src=".github/assets/readme/hero-light.webp" width="100%" alt="Assiya：让装备上的属性在游戏里生效；配置驱动属性的格式与行为。右侧为游戏内属性详情截图。">
  </picture>
</p>

<p align="center">面向 RPG 玩法的 Minecraft 属性插件，运行于 Paper 服务端。</p>

<p align="center">
  <img alt="Paper" src="https://img.shields.io/badge/平台-Paper-527AAB?style=flat-square&labelColor=29354D">
  <img alt="Kotlin 2.4" src="https://img.shields.io/badge/Kotlin-2.4-527AAB?style=flat-square&logo=kotlin&logoColor=white&labelColor=29354D">
  <a href="LICENSE"><img alt="许可证 GPL v3" src="https://img.shields.io/badge/许可证-GPL_v3-527AAB?style=flat-square&labelColor=29354D"></a>
</p>

<p align="center">
  <a href="./assiya-docs/content/guide/intro.md">项目介绍</a>&ensp;·&ensp;<a href="./assiya-docs/content/guide/quick-start.md">快速开始</a>&ensp;·&ensp;<a href="./assiya-docs/content/mythicmobs/mob-attributes.md">MythicMobs</a>
</p>

<p align="center">
  <img src=".github/assets/divider.svg" width="100%" alt="分割线" />
</p>

<br>

<h3 align="center">物品上写属性，游戏里看结果</h3>

<p align="center">默认配置下，在 Lore 中写入 <code>攻击力: 100</code>，手持物品就能读取。</p>

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset=".github/assets/readme/reading-dark.webp">
    <img src=".github/assets/readme/reading-light.webp" width="100%" alt="左侧为带有攻击力: 100 的物品 Lore 示例，右侧为游戏内属性列表截图。">
  </picture>
</p>

<p align="center">
  执行 <code>/assiya status &lt;玩家名&gt;</code> 查看当前属性，悬停查看读到的数值和计算结果。<br>
  <sub>百分比、随机范围和自定义格式的写法，见<a href="./assiya-docs/content/guide/quick-start.md">为物品添加属性</a>。</sub>
</p>

<br>

<h3 align="center">读取格式和生效条件，由你定义</h3>

<p align="center">
  从 Lore 或 NBT 读取属性，通过读取组定义数值格式。<br>
  穿戴等级、槽位限制、计算公式，都可以按服务器的玩法配置。
</p>

<p align="center">
  <a href="./assiya-docs/content/config/readers.md">读取组与自定义格式</a>&emsp;·&emsp;<a href="./assiya-docs/content/config/conditions.md">使用条件与生效范围</a>
</p>

<br>

<h3 align="center">属性生效时做什么，用脚本来写</h3>

<p align="center">
  内置 JavaScript 和 Groovy 支持，用脚本编写战斗属性和触发行为。<br>
  同时提供 MythicMobs 4 / 5 的怪物属性、技能机制、条件与占位符。
</p>

<p align="center">
  <a href="./assiya-docs/content/tutorial/attribute.md">编写一个增伤属性</a>&emsp;·&emsp;<a href="./assiya-docs/content/mythicmobs/mob-attributes.md">为 MythicMobs 怪物添加属性</a>
</p>

<br>

<p align="center">
  <img src=".github/assets/divider.svg" width="100%" alt="分割线" />
</p>

<a id="download"></a>
<h3 align="center">下载 Assiya</h3>

<p align="center">前往 GitHub Releases 下载。</p>

<p align="center">
  <img alt="GitHub Releases 下载 JAR" src="https://img.shields.io/badge/GitHub%20Releases-%E4%B8%8B%E8%BD%BD%20JAR%20%C2%B7%20TODO-527AAB?style=for-the-badge&logo=github&logoColor=white&labelColor=29354D">
</p>

<p align="center">
  <sub>首次使用请先阅读<a href="./assiya-docs/content/guide/quick-start.md">快速开始</a>，了解依赖插件与安装步骤。</sub>
</p>

<br>

<h3 align="center">鸣谢与许可证</h3>

<p align="center">
  <a href="https://github.com/Skillw/AttributeSystem">AttributeSystem</a>：读取组与 NBT 结构的设计参考。<br>
  Assiya 插件代码采用 <a href="./LICENSE">GPL v3 开源协议</a>
</p>