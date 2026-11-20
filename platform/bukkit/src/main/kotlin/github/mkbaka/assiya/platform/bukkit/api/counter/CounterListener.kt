package github.mkbaka.assiya.platform.bukkit.api.counter

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent

object CounterListener {

    @Listener(priority = EventPriority.MONITOR)
    fun onDeath(event: EntityDeathEvent) {
        AssiyaAPI.getSession(event.entity)?.counter?.resetDeath()
    }

}