package github.mkbaka.assiya.platform.bukkit.api.compat

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity

object FoliaCompat {

    private val isFolia = try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false, FoliaCompat::class.java.classLoader)
        true
    } catch (_: ClassNotFoundException) {
        false
    }

    /**
     * 当前是否运行于 Folia 环境
     */
    @JvmStatic
    fun isFolia(): Boolean = isFolia

    /**
     * 提交基于 entity scheduler 的任务
     */
    fun scheduleEntity(entity: Entity, ticks: Long, action: Runnable, retired: Runnable): Runnable {
        check(isFolia) { "Entity region dispatch requires Folia" }
        val task = entity.scheduler.runDelayed(
            AssiyaBukkitPlugin.INSTANCE, { action.run() }, retired, ticks.coerceAtLeast(1L)
        )
        if (task == null) retired.run()
        return Runnable { task?.cancel() }
    }

    /**
     * 提交基于 region scheduler 的任务
     */
    fun scheduleRegion(world: World, chunkX: Int, chunkZ: Int, ticks: Long, action: Runnable): Runnable {
        check(isFolia) { "Region dispatch requires Folia" }
        val task = Bukkit.getRegionScheduler().runDelayed(
            AssiyaBukkitPlugin.INSTANCE, world, chunkX, chunkZ,
            { action.run() }, ticks.coerceAtLeast(1L)
        )
        return Runnable { task.cancel() }
    }

    /**
     * 提交基于 global scheduler 的任务
     */
    fun scheduleGlobal(delay: Long, period: Long, action: Runnable): Runnable {
        check(isFolia) { "Global tick dispatch requires Folia" }
        val scheduler = Bukkit.getGlobalRegionScheduler()
        val task = if (period > 0L) {
            scheduler.runAtFixedRate(AssiyaBukkitPlugin.INSTANCE, { action.run() }, delay.coerceAtLeast(1L), period)
        } else {
            scheduler.runDelayed(AssiyaBukkitPlugin.INSTANCE, { action.run() }, delay.coerceAtLeast(1L))
        }
        return Runnable { task.cancel() }
    }

    /**
     * 能否在当前线程操作目标实体
     */
    @JvmStatic
    fun isOwnedByCurrentRegion(entity: Entity): Boolean = Bukkit.isOwnedByCurrentRegion(entity)

}
