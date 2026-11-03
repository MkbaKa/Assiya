package scripts.condition

import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.api.condition.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import groovy.transform.CompileStatic
import org.bukkit.entity.Entity

@CompileStatic
static boolean check(Condition condition, Entity entity, ConditionContextImpl context) {
    BukkitEquipmentSlot slot = context.equipmentSlot
    if (slot == null) return false

    OptionsComponent config = condition.get(OptionsComponent.@Key)
    if (config == null) return false
    List<String> slotNames = config.getStringList(slot.name())

    String value = context.capture("value")
    if (value == null) return false

    for (String require : value.split("/")) {
        if (slotNames.contains(require)) return true
    }
    return false
}
