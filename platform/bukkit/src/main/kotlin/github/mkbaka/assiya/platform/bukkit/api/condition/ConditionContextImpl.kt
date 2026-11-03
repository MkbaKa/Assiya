package github.mkbaka.assiya.platform.bukkit.api.condition

import github.mkbaka.assiya.common.condition.ConditionContext
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import org.bukkit.inventory.ItemStack

class ConditionContextImpl(
    captures: Map<String, Any> = emptyMap(),
    val equipmentSlot: BukkitEquipmentSlot? = null,
    val itemStack: ItemStack? = null,
    val sourceId: String? = null
) : ConditionContext(captures) {

    fun withCaptures(captures: Map<String, Any>): ConditionContextImpl {
        return ConditionContextImpl(captures, equipmentSlot, itemStack, sourceId)
    }

}
