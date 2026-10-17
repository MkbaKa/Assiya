package github.mkbaka.assiya.platform.bukkit.internal.parser

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
