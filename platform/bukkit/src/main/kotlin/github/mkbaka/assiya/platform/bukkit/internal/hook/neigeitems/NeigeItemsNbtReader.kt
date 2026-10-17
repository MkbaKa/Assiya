package github.mkbaka.assiya.platform.bukkit.internal.hook.neigeitems

import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.MapAttributeSourcePart
import github.mkbaka.assiya.platform.bukkit.api.reader.ItemReader
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.utils.ItemUtils.getNbtOrNull
import pers.neige.neigeitems.utils.ItemUtils.toValue

object NeigeItemsNbtReader : ItemReader {

    override fun read(item: ItemStack): AttributeSourcePart? {
        val tag = item.getNbtOrNull() ?: return null

        val data = tag.getCompound("assiya") ?: return null

        val attributes = data.getCompound("attributes")
        val conditions = data.getList("conditions")
        if (attributes == null && conditions == null) return null

        return MapAttributeSourcePart.fromMap(
            attributes?.toValue() as? Map<*, *> ?: emptyMap<String, Any>(),
            conditions?.toValue() as? List<*> ?: emptyList<Any>()
        )
    }

}
