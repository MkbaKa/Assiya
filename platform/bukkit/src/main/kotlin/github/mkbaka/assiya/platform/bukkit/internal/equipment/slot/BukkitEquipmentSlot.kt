package github.mkbaka.assiya.platform.bukkit.internal.equipment.slot

import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

enum class BukkitEquipmentSlot(
    val slot: EquipmentSlot,
    val source: String,
    private val getter: (EntityEquipment) -> ItemStack?
) {

    // 1.12.2 没有 org.bukkit.inventory.EntityEquipment#getItem(@NotNull EquipmentSlot slot)
    MAIN_HAND(EquipmentSlot.HAND, "main_hand", EntityEquipment::getItemInMainHand),
    OFF_HAND(EquipmentSlot.OFF_HAND, "off_hand", EntityEquipment::getItemInOffHand),

    HEAD(EquipmentSlot.HEAD, "head", EntityEquipment::getHelmet),
    CHEST(EquipmentSlot.CHEST, "chest", EntityEquipment::getChestplate),
    LEGS(EquipmentSlot.LEGS, "legs", EntityEquipment::getLeggings),
    FEET(EquipmentSlot.FEET, "feet", EntityEquipment::getBoots)
    ;

    val sourceId = "equipment:$source"

    fun getSlotItem(equipment: EntityEquipment?): ItemStack? = equipment?.let(getter)

    companion object {

        @JvmStatic
        fun <T> map(transformer: (BukkitEquipmentSlot) -> T): List<T> = entries.map(transformer)

        @JvmStatic
        fun <T> mapNotNull(transformer: (BukkitEquipmentSlot) -> T?): List<T> = entries.mapNotNull(transformer)

    }

}
