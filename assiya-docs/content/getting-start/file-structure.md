# 文件结构

当插件成功加载后，生成的默认配置结构大概是这样的：

```text
Assiya
├─ config.yml
├─ attribute
│  ├─ ...
├─ condition
│  └─ ...
├─ formula
│  └─ ...
├─ lang
│  └─ ...
├─ reader
│  └─ ...
└─ scripts
   └─ ...
```

## 配置文件

(`config.yml`) 提供了一些 Assiya 全局上的设置项，控制对应功能的开关、参数。  
其中的篇幅较长，见[配置页面](TODO)。

(`attribute`) 用于编写属于自己的属性，见[属性](TODO)。

(`condition`) 负责提供解析属性的条件，见[条件](TODO)。

(`formula`) 是属性实现中引用的计算公式目录，见[公式计算](TODO)。

(`lang`) 提供消息翻译，没什么好说的。

(`reader`) 提供属性、条件在文本中的解析规则，见[读取](TODO)。

(`scripts`) 存放自定义脚本、属性或条件等各种具体实现，见[脚本](TODO)。

## 下一步

- 想要进一步了解 Assiya 提供的默认属性，可以看[属性](TODO)
- 直接尝试使用属性？[为物品增加属性](./item-attribute.md)