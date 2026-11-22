package github.mkbaka.assiya.platform.bukkit.internal.equipment.slot

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 解析穿戴目标槽位
 *
 * 1.21.3+ 新增组件系统后则基于组件判断
 * 反之基于物品材质
 */
internal interface WearableSlotSource {

    fun resolve(item: ItemStack): BukkitEquipmentSlot?

    /**
     * 右键穿戴
     */
    fun resolveForInteraction(item: ItemStack, player: Player): BukkitEquipmentSlot? {
        val slot = resolve(item) ?: return null
        val equipped = slot.getSlotItem(player.equipment)
        return if (equipped == null || equipped.type == Material.AIR) slot else null
    }

}
