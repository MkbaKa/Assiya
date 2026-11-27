package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

internal object RegionDispatcher : CoroutineDispatcher() {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        val region = context[SchedulerContext] as RegionContext
        return if (FoliaCompat.isFolia()) {
            !Bukkit.isOwnedByCurrentRegion(region.world, region.chunkX, region.chunkZ)
        } else {
            !Bukkit.isPrimaryThread()
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (FoliaCompat.isFolia()) {
            val region = context[SchedulerContext] as RegionContext
            Bukkit.getRegionScheduler().run(
                AssiyaBukkitPlugin.INSTANCE, region.world, region.chunkX, region.chunkZ
            ) { block.run() }
        } else {
            Bukkit.getScheduler().runTask(AssiyaBukkitPlugin.INSTANCE, block)
        }
    }

}
