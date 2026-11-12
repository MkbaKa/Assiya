# 编写一个增伤属性

[前文中](../config/attributes.md)列出了属性配置能写哪些参数，但没有编写过属性的你，显然不知道它们具体影响了什么。

假设我们要编写一个叫做`附加伤害`的增伤类属性，应该怎么做？

## 首先，要让 Assiya 认识这个属性

在 `attribute/` 目录下新建或者找个现成的配置文件写入：

```yaml
extra-damage:
  names: ['附加伤害']
  reader: default
```

- `extra-damage` 是我们给属性取的 **属性 id**，不能和其他属性重复。
- `names` 是物品 Lore 中的读取名，`reader: default` 代表使用默认读取组。

只有在指定读取组后，物品上的 `附加伤害: 10` 才会被识别为 `extra-damage` 属性的数值。

此时执行 `/assiya reload`，给手里的武器加上一行 `附加伤害: 10`，切换一下手持物品后执行 `/assiya status <玩家名>`，就能看到附加伤害的 `value` 和 `total` 都是 `10`。接下来我们给它加上攻击时的效果。

## 编写属性对应的逻辑

在 `plugins/Assiya/scripts/attribute/fight` 目录下新建 `extra-damage.groovy`：

```groovy :line-numbers
package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    double value = attacker.getValue(attribute.id, 'total')

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    damage.addDamage(attribute.id, value)
}
```

这段代码实现了两个功能：

### 1. 读取攻击方的属性值

```groovy
double value = attacker.getValue(attribute.id, 'total')
```

- `attribute.id` 就是配置里的 `extra-damage`。这里传 `attribute.id` 而不是写死 `extra-damage`，是为了以后其他属性复用这份脚本时也能读到各自的属性值。
- `'total'` 是默认读取组算出来的最终值（会把固定值 `value`、范围值和百分比 `percent` 合在一起算，详见[读取组与自定义格式](../config/readers.md)）。

假设我们拥有 `100` 点附加伤害，这里拿到的就是 `100`，如果有 `200` 点拿到的就是 `200`。

### 2. 把数值加入本次伤害

```groovy
damage.addDamage(attribute.id, value)
```

在本次过程中，负责统计伤害的组件里保存一条叫做 `extra-damage` 的伤害，对应的数值是 `value` 变量的值。

## 把属性配置和脚本连接起来

回到 `extra-damage.yml`，补上脚本路径、优先级和触发器：

```yaml :line-numbers
extra-damage:
  names: ["附加伤害"]
  reader: default
  // [!code ++:3]
  handler: attribute/fight/extra-damage
  priority: 130
  triggers: attack
```

- `handler` 填写相对于 `scripts` 目录的路径（去掉 `.groovy` 扩展名）。
- `triggers: attack` 表示在攻击时调用脚本里的 `onAttack` 方法。
- `priority: 130` 决定它在一次攻击中的执行顺序。默认物理伤害是 `100`，物理防御是 `200`，填 `130` 会让它在两者之间执行。

:::tip 为什么排在物理防御前面，却不会被物理防御减免？
因为默认物理防御只扣减伤害 id 为 `physical-attack` 的那一条伤害，而我们刚才加入的伤害 id 是 `extra-damage`。触发顺序见[伤害类触发](trigger-attack.md)。
:::

执行 `/assiya reload`，拿起带有 `附加伤害: 10` 的武器砍向目标，这 10 点附加伤害就会参与攻击结算了。

## 在基础例子上继续扩展

### 扩展 1：增加别名或让其他属性复用脚本

想让 `额外伤害: 10` 也算作同一个属性，直接在 `names` 里加一个名字就可以了：

```yaml
names: ["附加伤害", "额外伤害"]
```

如果想新增一个“魔法伤害”属性，效果同样是直接把数值加进伤害组件，甚至不需要新建脚本，直接复用刚才的 `handler` 即可：

```yaml :line-numbers
magic-damage:
  names: ["魔法伤害"]
  reader: default
  handler: attribute/fight/extra-damage
  priority: 140
  triggers: attack
```

### 扩展 2：把计算式抽到公式文件

假设我们想再做一个“附加伤害倍率”属性，按 `附加伤害 × (1 + 倍率 / 100)` 计算，并且希望服主以后不用改 Groovy 代码就能在配置里调整这条算式。

1. 在 `extra-damage.yml` 中多声明一个只存数值、不绑脚本的属性 `extra-damage-rate`：

```yaml :line-numbers
extra-damage-rate:
  names: ["附加伤害倍率"]
  reader: default
```

2. 在 `plugins/Assiya/formula` 下新建 `custom.yml`，写上计算式：

```yaml
extra-damage: '<base> * (1 + <bonus> / 100)'
```

3. 在 `extra-damage.groovy` 中用 `processor.formula` 调用它：

```groovy :line-numbers
package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    double value = attacker.getValue(attribute.id, 'total')
    // [!code ++:6]
    double bonus = attacker.getValue('extra-damage-rate', 'total', 0.0d)

    double result = processor.formula('extra-damage') { Map<String, Object> variables ->
        variables.put('base', value)
        variables.put('bonus', bonus)
    }

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    // [!code --]
    damage.addDamage(attribute.id, value)
    // [!code ++]
    damage.addDamage(attribute.id, result)
}
```

`getValue` 的第三个参数 `0.0d` 表示当玩家没有“附加伤害倍率”属性时默认按 `0` 算。脚本里 `variables.put` 塞进去的 `base` 和 `bonus`，就对应公式配置里的 `<base>` 和 `<bonus>`。

### 扩展 3：通过 `options` 与 `callback` 发送战斗提示

如果希望触发附加伤害时向攻击方和受击方发送聊天栏提示，并且提示语可以在 YAML 里自由修改，可以在属性下增加 `options` 节点：

```yaml :line-numbers
extra-damage:
  names: ["附加伤害"]
  reader: default
  handler: attribute/fight/extra-damage
  priority: 130
  triggers: attack
  // [!code ++:3]
  options:
    attacker: '你对 {entity} 增加了 {extra} 点附加伤害，本次结算总伤害为 {total}。'
    entity: '{attacker} 对你增加了 {extra} 点附加伤害，本次结算总伤害为 {total}。'
```

然后在 `extra-damage.groovy` 中登记一个 `callback` 回调来发送消息：

```groovy :line-numbers
package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
// [!code ++]
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
// [!code ++:2]
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic

@CompileStatic
static void onAttack(Attribute attribute, BukkitProcessor processor) {
    TriggerComponent attacker = processor.get(TriggerComponent.@Key)
    if (attacker == null) return

    double value = attacker.getValue(attribute.id, 'total')

    DamageMultipleComponent damage = processor.get(DamageMultipleComponent.@Key)
    damage.addDamage(attribute.id, value)

    // [!code ++:10]
    processor.get(CallbackComponent.@Key)?.callback {
        ScriptTools.sendCombatMessage(
                attribute,
                processor,
                [
                    extra: StringUtils.format(value, 2),
                    total: StringUtils.format(damage.totalDamage, 2)
                ]
        )
    }
}
```

- `ScriptTools.sendCombatMessage` 会自动读取当前属性配置下的 `options.attacker` 和 `options.entity`，并自带 `{attacker}` 和 `{entity}` 两个名字变量。
- 传入的 `extra` 和 `total` 对应提示语里的 `{extra}` 和 `{total}`，并用 `StringUtils.format(..., 2)` 保留两位小数。
- **为什么要放进 `callback` 里发？** 因为优先级 `130` 执行时，排在后面的物理防御（`200`）、护甲（`250`）、暴击（`300`）都还没执行，闪避甚至可能取消整次攻击。放进 `callback` 可以等所有属性脚本都算完之后再读取 `damage.totalDamage`，保证玩家看到的总伤害是最终结算值。
