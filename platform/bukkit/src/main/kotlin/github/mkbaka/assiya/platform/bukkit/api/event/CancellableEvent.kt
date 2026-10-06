package github.mkbaka.assiya.platform.bukkit.api.event

import org.bukkit.event.Cancellable

class CancellableEvent : Cancellable {

    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}