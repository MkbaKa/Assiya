# 从 AttributePlus 迁移

在安装 [Release](https://github.com/MkbaKa/Assiya/releases) 里提供的 `Assiya-AttributePlus` 插件后，它会提供一系列降低迁移难度的功能。

比如，它将一些能够复用的 API 转接到了 Assiya 实现，这样可以避免一些附属开发者要针对 Assiya 重复编写兼容代码。

并且它保留了 AttributePlus 的 `script.yml` 与原目录下 `script` 目录里脚本的解析兼容，它会在 Assiya 重载脚本模块时，注册对应的 AP 脚本属性。

它可以覆盖 90% 的兼容需求，只有像 `PresetAPI`、`Custom 触发器` 之类的特殊情况无法自动适配。

如果你想知道自己的脚本、附属需不需要单独针对 Assiya 提供兼容，可以让你的 `Agent` 阅读项目代码里的 `compact-attributeplus` 核对缺失、不支持的 API 细节。