# 战斗状态 InCombat

<SyntaxBar
  label="条件示例"
  syntax="- assiya:in-combat true"
  :aliases="['assiya:in-combat', 'assiya:incombat']"
  badge="实体条件"
/>

判断被检查的玩家是否处于 Assiya 的战斗状态，无需参数。战斗状态的进入和退出见[战斗状态](../../tutorial/combat-state.md)。

非玩家实体或没有 Assiya 数据的玩家会返回 `false`。

## 示例配置

要求玩家处于战斗状态：

```yaml
Conditions:
  - assiya:in-combat true
```

要求玩家未处于战斗状态：

```yaml
Conditions:
  - assiya:in-combat false
```

`false` 会反转条件结果，因此非玩家实体也会通过第二个示例。
