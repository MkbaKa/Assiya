package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.platform.bukkit.api.event.util.CancellableEvent
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

sealed class CombatJoinEvent(
    val session: AssiyaSession,
    val target: Entity?
) : Event(!Bukkit.isPrimaryThread()) {

    class Pre(session: AssiyaSession, target: Entity?) : CombatJoinEvent(session, target),
        Cancellable by CancellableEvent()

    class Post(session: AssiyaSession, target: Entity?) : CombatJoinEvent(session, target)

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {

        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS

    }

}