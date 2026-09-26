package github.mkbaka.assiya.platform.bukkit.internal.listener

import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerItemHeldEvent

object EquipmentListener {

    @Listener(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun held(event: PlayerItemHeldEvent) {
        EquipmentPipeline.refresh(event.player)
    }

}