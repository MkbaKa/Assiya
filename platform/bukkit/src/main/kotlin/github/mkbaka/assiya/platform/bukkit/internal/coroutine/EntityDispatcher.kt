package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

internal object EntityDispatcher : CoroutineDispatcher() {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        val entity = (context[SchedulerContext] as EntityContext).entity
        return if (FoliaCompat.isFolia()) {
            !FoliaCompat.isOwnedByCurrentRegion(entity)
        } else {
            !Bukkit.isPrimaryThread()
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (FoliaCompat.isFolia()) {
            val entity = (context[SchedulerContext] as EntityContext).entity
            FoliaCompat.scheduleEntity(entity, 1L, block) {
                context.cancel(CancellationException("Entity scheduler was retired"))
                Dispatchers.IO.dispatch(context, block)
            }
        } else {
            Bukkit.getScheduler().runTask(AssiyaBukkitPlugin.INSTANCE, block)
        }
    }

}
