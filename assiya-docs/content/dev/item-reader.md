# 对单个物品的解析

向 [ItemReaderRegistry](https://github.com/MkbaKa/Assiya/blob/main/platform/bukkit/src/main/kotlin/github/mkbaka/assiya/platform/bukkit/internal/registry/ItemReaderRegistry.kt) 中注册你的读取实现即可

## 实现

Assiya 提供了两种物品读取方式，一种是普通的 Lore 文本读取，另一种为基于 NeigeItems 实现的 [NBT 读取](../config/nbt.md)。

下文是如何把 ItemStack#Lore 变成解析结果的实现

```kotlin
import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.TextAttributeSourcePart
import github.mkbaka.assiya.platform.bukkit.api.reader.ItemReader
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.uncolored
import org.bukkit.inventory.ItemStack

object LoreItemReader : ItemReader {

    override fun read(item: ItemStack): AttributeSourcePart? {
        val lore = item.itemMeta?.lore.orEmpty()

        return when {
            lore.isEmpty() -> null

            else -> TextAttributeSourcePart(lore.uncolored())
        }
    }

}

```