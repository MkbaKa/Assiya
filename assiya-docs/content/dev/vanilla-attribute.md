# 原版属性操作

`VanillaAttributes` 用于读取和修改实体的原版属性，例如生命上限、移动速度和护甲。它包装了 Bukkit 的属性实例，并提供按来源标识设置、查询和移除修饰器的方法。

## 获取属性

通过 `VanillaAttributes.of(entity)` 创建包装，再获取对应的 `VanillaAttributeView`：

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import org.bukkit.entity.LivingEntity

fun readMaxHealth(entity: LivingEntity): Double {
    val vanilla = VanillaAttributes.of(entity)
    val health = vanilla.maxHealth

    // 原版属性的基础值
    val base = health.base
    // 计入修饰器后的实际值
    return health.value
}
```

常用属性有对应的快捷入口：

| 入口 | 原版属性 |
| --- | --- |
| `maxHealth` | 生命上限 |
| `movementSpeed` | 移动速度 |
| `attackDamage` | 攻击伤害 |
| `attackSpeed` | 攻击速度 |
| `armor` | 护甲 |
| `armorToughness` | 护甲韧性 |
| `knockbackResistance` | 击退抗性 |
| `followRange` | 生物追踪目标的范围 |
| `luck` | 幸运值 |
| `scale` | 实体体型缩放比例 |
| `gravity` | 重力 |
| `jumpStrength` | 跳跃强度 |

也可以传入当前服务端提供的 Bukkit `Attribute`，通过 `vanilla[attribute]` 获取。

### 按名称查找

对于可能受服务端版本或实体种类限制的属性，可以使用 `find`：

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import org.bukkit.entity.LivingEntity

fun readScale(entity: LivingEntity): Double? {
    val scale = VanillaAttributes.of(entity).find("scale") ?: return null
    return scale.value
}
```

名称不区分大小写，兼容旧版的 `GENERIC_`、`PLAYER_`、`ZOMBIE_` 和 `HORSE_` 前缀。例如 `max_health` 与 `GENERIC_MAX_HEALTH` 都可以用于查找生命上限。

`find` 在服务端不支持该属性，或实体没有该属性时返回 `null`。快捷入口与 `vanilla[attribute]` 则要求属性存在，否则抛出异常。包装提供了入口，并不代表所有版本、所有实体都拥有对应属性。

## 修改基础值

`base` 可以直接赋值，`value` 只用于读取实际生效的值：

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import org.bukkit.entity.LivingEntity

fun setBaseMaxHealth(entity: LivingEntity, amount: Double) {
    val health = VanillaAttributes.of(entity).maxHealth
    health.base = amount

    // 降低生命上限后, 同步限制当前生命值
    if (entity.health > health.value) {
        entity.health = health.value
    }
}
```

修改 `base` 会改变该原版属性的基础值。要为自己的功能增加一份可以独立更新、移除的加成，使用修饰器更方便。

## 设置修饰器

修饰器使用 `source` 区分来源。建议使用自己的命名空间，例如 `example:max-health`，同一实体、同一原版属性上的不同功能使用不同标识。

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity

fun updateHealthBonus(entity: LivingEntity, amount: Double) {
    val health = VanillaAttributes.of(entity).maxHealth
    val source = "example:max-health"

    if (amount > 0.0) {
        // 替换同来源的旧修饰器, 重复调用不会叠加
        health.setTransient(source, amount, AttributeModifier.Operation.ADD_NUMBER)
    } else {
        health.removeModifier(source)
    }

    if (entity.health > health.value) {
        entity.health = health.value
    }
}
```

`source` 需要符合 `namespace:key` 格式，命名空间使用小写字母、数字、`_`、`-` 或 `.`，键还可以使用 `/`。内部会根据服务端 API，将它转换为命名空间键或稳定的 UUID。

### 设置与添加

| 方法 | 同来源修饰器已存在时 | 保存方式 |
| --- | --- | --- |
| `setTransient` | 替换旧修饰器 | 临时修饰器 |
| `addTransient` | 抛出异常 | 临时修饰器 |
| `setPermanent` | 替换旧修饰器 | 普通持久修饰器 |
| `addPermanent` | 抛出异常 | 普通持久修饰器 |

这些方法都接收 `source`、`amount` 和 `operation`。`add` 表示新增一个来源，并不会将数值累加到已有的同来源修饰器中。属性更新时需要反复写入最新结果，通常使用 `setTransient`。

临时修饰器优先使用服务端的 `addTransientModifier`，不随实体数据保存。旧版 API 不支持时，Assiya 会添加普通修饰器并记录，在玩家退出、非玩家实体移出世界或 Assiya 停用时移除。

持久修饰器使用普通的 `addModifier`，可随实体数据保存，不进入上述临时修饰器清理流程。它是否继续存在，也取决于后续是否被其他逻辑替换或移除。

### 运算方式

`operation` 使用 Bukkit 的 `AttributeModifier.Operation`：

| 运算 | 含义 |
| --- | --- |
| `ADD_NUMBER` | 直接增加数值。 |
| `ADD_SCALAR` | 按完成 `ADD_NUMBER` 运算后的值增加比例，多个此类修饰器的比例相加。 |
| `MULTIPLY_SCALAR_1` | 在前两类运算后乘以 `1 + amount`，多个此类修饰器依次相乘。 |

例如基础值为 `20`，先通过 `ADD_NUMBER` 增加 `10`，再通过 `ADD_SCALAR` 增加 `0.5`，最后通过 `MULTIPLY_SCALAR_1` 增加 `0.2`，结果为：

```text
(20 + 10) × (1 + 0.5) × (1 + 0.2) = 54
```

原版属性自身的上下限也会影响最终的 `value`。

## 查询与移除

使用与设置时相同的 `source` 操作自己的修饰器：

```kotlin :line-numbers
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import org.bukkit.entity.LivingEntity

fun removeHealthBonus(entity: LivingEntity): Boolean {
    val health = VanillaAttributes.of(entity).maxHealth
    val source = "example:max-health"

    // 不存在时返回 null
    val modifier = health.getModifier(source) ?: return false

    // 移除成功返回 true, 不存在时返回 false
    val removed = health.removeModifier(source)
    if (entity.health > health.value) {
        entity.health = health.value
    }
    return removed
}
```

`removeModifier` 同时适用于临时和持久修饰器，只移除该来源对应的修饰器。

::: warning 在实体允许的线程中操作
`VanillaAttributes` 不是线程安全的包装。读取与修改都直接访问实体的 Bukkit 属性实例，应在实体允许的线程中执行，异步计算完成后再切回对应线程应用结果。
:::
