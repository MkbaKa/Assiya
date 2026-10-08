package github.mkbaka.assiya.platform.bukkit.internal.session

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object AssiyaSessionListener {

    @Listener
    fun onJoin(event: PlayerJoinEvent) {
        if (Config.global.HEALTH_SCALE) {
            event.player.healthScale = Config.global.HEALTH_SCALE_VALUE
        }

        AssiyaAPI.getSessionOrCreate(event.player)
    }

    @Listener
    fun onQuit(event: PlayerQuitEvent) {
        AssiyaSessionRegistry.remove(event.player.uniqueId)
    }

    @Listener(priority = EventPriority.MONITOR)
    fun onRemove(event: EntityRemoveFromWorldEvent) {
        val entity = event.entity
        if (entity is Player) return

        AssiyaSessionRegistry.remove(event.entity.uniqueId)
    }

}