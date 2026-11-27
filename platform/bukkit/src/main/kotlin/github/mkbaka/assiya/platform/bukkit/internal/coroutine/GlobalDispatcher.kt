package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

internal object GlobalDispatcher : CoroutineDispatcher() {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return if (FoliaCompat.isFolia()) !Bukkit.isGlobalTickThread() else !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (FoliaCompat.isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(AssiyaBukkitPlugin.INSTANCE) { block.run() }
        } else {
            Bukkit.getScheduler().runTask(AssiyaBukkitPlugin.INSTANCE, block)
        }
    }

}
