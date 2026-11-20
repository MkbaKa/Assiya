package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadModule
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

sealed class ModuleReloadEvent(val module: ReloadModule) : Event(!Bukkit.isPrimaryThread()) {

    class Pre(module: ReloadModule, requiresAttributeRefresh: Boolean = false) : ModuleReloadEvent(module) {

        var requiresAttributeRefresh: Boolean = requiresAttributeRefresh
            private set

        fun requireAttributeRefresh() {
            requiresAttributeRefresh = true
        }

    }

    class Post(module: ReloadModule) : ModuleReloadEvent(module)

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }

}
