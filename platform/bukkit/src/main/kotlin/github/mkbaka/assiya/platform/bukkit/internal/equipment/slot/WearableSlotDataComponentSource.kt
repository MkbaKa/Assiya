package github.mkbaka.assiya.platform.bukkit.internal.equipment.slot

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
internal class WearableSlotDataComponentSource : WearableSlotSource {

    override fun resolve(item: ItemStack): BukkitEquipmentSlot? {
        val equippable: Equippable = item.getData(DataComponentTypes.EQUIPPABLE) ?: return null

        return resolveSlot(equippable)
    }

    override fun resolveForInteraction(item: ItemStack, player: Player): BukkitEquipmentSlot? {
        val equippable = item.getData(DataComponentTypes.EQUIPPABLE) ?: return null
        if (!equippable.swappable()) return null

        val allowedEntities = equippable.allowedEntities()
        if (allowedEntities != null
            && !allowedEntities.contains(TypedKey.create(RegistryKey.ENTITY_TYPE, player.type.key))
        ) {
            return null
        }

        return resolveSlot(equippable)
    }

    private fun resolveSlot(equippable: Equippable): BukkitEquipmentSlot? {
        return when (equippable.slot().name) {
            "HEAD" -> BukkitEquipmentSlot.HEAD
            "CHEST" -> BukkitEquipmentSlot.CHEST
            "LEGS" -> BukkitEquipmentSlot.LEGS
            "FEET" -> BukkitEquipmentSlot.FEET
            "HAND" -> BukkitEquipmentSlot.MAIN_HAND
            "OFF_HAND" -> BukkitEquipmentSlot.OFF_HAND

            else -> null
        }
    }

}
