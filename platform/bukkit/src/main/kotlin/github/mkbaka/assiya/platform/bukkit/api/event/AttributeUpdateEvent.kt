package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AttributeUpdateEvent(
    val entity: Entity,
    val context: AttributeUpdateContext
) : Event(!Bukkit.isPrimaryThread()), Cancellable {

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {

        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS

    }

}