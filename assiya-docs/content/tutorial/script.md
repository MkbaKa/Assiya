# 脚本环境

Assiya 默认只提供了 `JavaScript` 以及 `Groovy` 两种脚本引擎。

并提供了一个 `ScriptTools` 类预设一些常用方法。

脚本内可以通过 `onEnable` 与 `onDisable` 两个函数在脚本加载、重载/卸载时执行一些操作。

比如：

```groovy :line-numbers
static void onEnable() {
    println("脚本加载了!")
}

static void onDisable() {
    println("脚本卸载了!")
}
```

## 两种脚本引擎

## JavaScript 

Assiya 的 `JavaScript` 是基于 `Nashorn` 引擎驱动的。通常情况下，我并不建议你在 Assiya 中使用 `js` 脚本来编写内容。

因为它的运行效率并不算好，而且支持的语法、特性也不全，保留它是为了方便旧脚本的迁移。

## Groovy

在 AI 如此发达的当下，我更建议转换为使用 `groovy` 来编写自己需要的脚本。

它的运行效率、可用语法特性都远超瘸了腿儿的 `Nashorn`，代价只是一点点重载时的编译速度。~~（其实是因为我也不知道有啥好用的脚本环境了）~~

不过 Assiya 也为它做了一些专项调整。

### 对于 groovy 脚本，Assiya 支持以下两种写法：

::: code-group
```groovy [简易版]:line-numbers
static void onEnable() {
    println("脚本加载了!")
}

static void onDisable() {
    println("脚本卸载了!")
}
```
```groovy [完整类声明] :line-numbers
class SomeClassName {

    static void onEnable() {
        println("脚本加载了!")
    }

    static void onDisable() {
        println("脚本卸载了!")
    }
    
}
```
:::