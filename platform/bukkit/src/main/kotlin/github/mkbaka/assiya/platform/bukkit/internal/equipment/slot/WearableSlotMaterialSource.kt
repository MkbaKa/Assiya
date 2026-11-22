package github.mkbaka.assiya.platform.bukkit.internal.equipment.slot

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

internal object WearableSlotMaterialSource : WearableSlotSource {

    private val wearableSlots = EnumMap<Material, BukkitEquipmentSlot>(Material::class.java).apply {
        for (material in Material.entries) {
            val name = material.name
            if (name.startsWith("LEGACY_")) continue

            val slot = when {
                name.endsWith("_HELMET") || name.endsWith("_SKULL")
                    || (name.endsWith("_HEAD") && !name.contains("PISTON")) -> BukkitEquipmentSlot.HEAD

                name.endsWith("_CHESTPLATE") || name == "ELYTRA" -> BukkitEquipmentSlot.CHEST
                name.endsWith("_LEGGINGS") -> BukkitEquipmentSlot.LEGS
                name.endsWith("_BOOTS") -> BukkitEquipmentSlot.FEET
                else -> continue
            }

            put(material, slot)
        }
    }

    override fun resolve(item: ItemStack): BukkitEquipmentSlot? = wearableSlots[item.type]

}
