# 属性 & 条件 & 读取组

通常情况下，Assiya 更推荐通过配置与脚本的配合去创建它们，因为 Assiya 提供了自由度较高的配置方式，重载、测试起来也比较方便。

通过代码注册比较麻烦，但如果你实在是想要通过代码去构造，我只建议使用 kotlin 编写。

## 属性

```kotlin [单属性注册] :line-numbers
fun registerExampleAttribute() {
    
    // 属性的 id  不能重复
    val id = "example-attribute"
    val attribute = SimpleAttribute(id) { builder ->
        // 添加一个读取组件
        builder += ReaderComponent(
            // 读取组 ID
            readerId = "default",
            // 读取名称
            names = listOf("测试属性", "第二个名字")
        )

        // 绑定处理逻辑
        builder += HandlerComponent(
            // 增加一个优先级为 100 的 ATTACK 触发器
            TriggerRegistry.ATTACK to TriggerHandler(
                settings = SimpleTriggerSettings(priority = 100),
            ) { processor ->
                // 触发后的属性逻辑
                // ...
                
                // 取可能存在的 Trigger 的 example-attribute 属性总值
                val attacker = processor[TriggerComponent] ?: return@TriggerHandler
                val value = attacker.getValue(id, "total") ?: 0.0
                // 塞进伤害里
                processor[DamageMultipleComponent]?.addDamage(id, value)
            },
            
            // 其他触发器
            // ...
        )

        // 自定义选项组件 供脚本里读一些用户配置啥的
        builder += OptionsComponent(
            "extra-damage" to false,
        )

    }

    // 注册后才进入属性与处理器注册表, 即可被读取与触发
    attribute.register()
    
}
```

## 条件

```kotlin :line-numbers
fun registerExampleCondition() {
    // example-level 是条件 id
    val condition = SimpleCondition("example-level", { builder ->
        // 添加一个读取组件
        builder += ReaderComponent(
            // 读取组 id 
            readerId = "default",
            // 读取名称
            names = listOf("等级限制", "所需等级")
        )
    }) { entity, context ->
        // 条件的判断逻辑 
        // 如果返回 false 则代表条件不通过
        // ...
        
        val player = entity as? Player ?: return@SimpleCondition true

        val requiredLevel = context.capture<Double>("value") ?: return@SimpleCondition true
        player.level.toDouble() >= requiredLevel
    }

    condition.register()
}
```

## 读取组

::: code-group
```kotlin [读取数值] :line-numbers
fun registerExampleReader() {
    val reader = SimpleReader("example-number") { builder ->
        // 默认使用 Value.Number 读取数值
        // 合并数值的时候会使用 NumberOperations.Add 累加
        builder.capture("value", NumberOperations.Add)

        // 名称在前, 如 "测试属性: 100"
        builder.pattern("""{name}[\s:：]*<value>""")
        // 数值在前,  "100 测试属性"
        builder.pattern("""<value>[\s:：]*{name}""")

        // 根据读取结果计算 total
        builder.placeholder("total") { status ->
            status["value"] as? Double ?: 0.0
        }
    }

    reader.register()
}
```
```kotlin [读取文本] :line-numbers
fun registerExampleReader() {
    val reader = SimpleReader("example-text") { builder ->
        // 基于文本类的 text 捕获组
        // 合并不同来源的属性时 会使用 TextOperations.Override 只取最后一个文本结构
        builder.capture("text", TextOperations.Override, Value.Text)
        builder.pattern("""{name}[\s:：]*<text>""")
        
        // 非数字类不支持 placeholder
        // builder.placeholder
    }

    reader.register()
}
```
```kotlin [新建一种值类型] :line-numbers
fun registerExampleReader() {
    // 新建一个 boolean 类型的值
    // 只读 true / false 两种文本
    val booleanValue = object : Value<Boolean> {
        // 正则内容
        override val regex = "(?:true|false)"
        
        // 默认值
        override val defaultValue = false

        // 解析文本为实际值
        override fun parse(raw: String): Boolean? {
            return raw.toBooleanStrictOrNull()
        }
    }
    
    // 如果要配置里也可以使用这个 boolean Value
    // 要调用 ValueRegistry#register

    // 不同来源属性值合并的行为
    val overrideBoolean = object : Operation<Boolean> {
        override fun merge(current: Boolean?, incoming: Boolean): Boolean {
            // 忽略 current 代表只取最后一次读取到的结果
            return incoming
        }
    }

    // 创建对应的 reader
    val reader = SimpleReader("example-boolean") { builder ->
        // 对应的捕获组、合并方式、值类型
        builder.capture("enabled", overrideBoolean, booleanValue)
        builder.pattern("""{name}[\s:：]*<enabled>""")
    }

    reader.register()
}
```
:::

## 批量注册

由于 Assiya 会在属性、条件、读取组注册后创建索引，所以如果像上文一样，重复的注册单个属性会出现极大性能开销。

对于这种情况，Assiya 提供了 `AttributeRegistration#batch` 批量注册操作：

```kotlin :line-numbers
fun register(
    attributes: List<Attribute>,
    conditions: List<Condition>,
    readers: List<Reader>
) {
    // 批量注册时不会一个个创建索引
    AttributeRegistration.batch {
        // 如果后面两个玩意有引用需要注册的 reader
        // 请把 reader 放在前面
        for (reader in readers) {
            reader.register()
        }
        for (condition in conditions) {
            condition.register()
        }
        for (attribute in attributes) {
            attribute.register()
        }
    }
    // 结束后会统一创建
}
```
