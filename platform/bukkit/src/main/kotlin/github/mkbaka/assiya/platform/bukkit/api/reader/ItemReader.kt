package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import org.bukkit.inventory.ItemStack

fun interface ItemReader {

    fun read(item: ItemStack): AttributeSourcePart?

}
