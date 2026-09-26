package github.mkbaka.assiya.platform.bukkit.internal.listener

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSessionRegistry
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import kotlin.uuid.Uuid

object AttributeSessionListener {

    @Listener
    fun onJoin(event: PlayerJoinEvent) {
        AttributeSessionRegistry.getOrCreate(event.player)
        EquipmentPipeline.refresh(event.player)
    }

    @Listener
    fun onQuit(event: PlayerQuitEvent) {
        remove(event.player.uniqueId)
    }

    @Listener(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is Player) return

        remove(event.entity.uniqueId)
    }

    @Listener(priority = EventPriority.MONITOR)
    fun onRemove(event: EntityRemoveFromWorldEvent) {
        val entity = event.entity
        if (entity is Player) return

        remove(event.entity.uniqueId)
    }

    private fun remove(uuid: UUID) {
        EquipmentPipeline.cancel(uuid)
        AttributeSessionRegistry.remove(uuid)
    }

}
