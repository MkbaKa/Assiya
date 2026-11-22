package github.mkbaka.assiya.platform.bukkit.internal.equipment.slot

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object WearableSlotResolver {

    private val source: WearableSlotSource = detectSource()

    private fun detectSource(): WearableSlotSource {
        try {
            Class.forName("io.papermc.paper.datacomponent.DataComponentTypes").getField("EQUIPPABLE")
        } catch (_: ReflectiveOperationException) {
            return WearableSlotMaterialSource
        }

        return WearableSlotDataComponentSource()
    }

    @JvmStatic
    fun resolve(item: ItemStack?): BukkitEquipmentSlot? = item?.let(source::resolve)

    @JvmStatic
    fun resolveForInteraction(item: ItemStack?, player: Player): BukkitEquipmentSlot? =
        item?.let { source.resolveForInteraction(it, player) }

}
