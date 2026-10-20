package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.platform.bukkit.api.reader.ItemReader
import github.mkbaka.assiya.platform.bukkit.internal.feature.LoreItemReader
import github.mkbaka.assiya.platform.bukkit.internal.util.isAir
import org.bukkit.inventory.ItemStack

object ItemReaderRegistry : NamedRegistry<ItemReader>("物品读取器") {

    init {
        register("lore", LoreItemReader)
    }

    fun read(item: ItemStack?): List<AttributeSourcePart> {
        if (item == null || item.isAir) return emptyList()

        val parts = arrayListOf<AttributeSourcePart>()
        for ((_, reader) in elements) {
            val part = reader.read(item) ?: continue
            parts.add(part)
        }

        return parts
    }

}
