package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CombatLeaveEvent(
    val session: AssiyaSession,
    val target: Entity?
) : Event(!Bukkit.isPrimaryThread()) {

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {

        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS

    }

}