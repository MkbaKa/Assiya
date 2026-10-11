package github.mkbaka.assiya.platform.bukkit.internal.script.tools

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.callSync
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class ScriptScheduler {

    private var async = false
    private var delay = -1L
    private var period = -1L
    private lateinit var handler: Runnable
    private var task: BukkitTask? = null

    fun setAsync(async: Boolean): ScriptScheduler {
        this.async = async
        return this
    }

    fun setDelay(delay: Long): ScriptScheduler {
        this.delay = delay.coerceAtLeast(-1L)
        return this
    }

    fun setPeriod(period: Long): ScriptScheduler {
        this.period = period.coerceAtLeast(-1L)
        return this
    }

    fun setHandler(handler: Runnable): ScriptScheduler {
        this.handler = handler
        return this
    }

    fun register(): ScriptScheduler {
        callSync {
            unregister()
            val runnable = object : BukkitRunnable() {
                override fun run() {
                    handler.run()
                }
            }
            val plugin = AssiyaBukkitPlugin.INSTANCE
            task = when {
                async && period > 0 -> runnable.runTaskTimerAsynchronously(plugin, delay.coerceAtLeast(0L), period)
                async && delay > 0 -> runnable.runTaskLaterAsynchronously(plugin, delay)

                period > 0 -> runnable.runTaskTimer(plugin, delay.coerceAtLeast(0L), period)
                delay > 0 -> runnable.runTaskLater(plugin, delay)

                async -> runnable.runTaskAsynchronously(plugin)
                else -> runnable.runTask(plugin)
            }
            schedulers.add(this)
        }
        return this
    }

    fun unregister(): ScriptScheduler {
        callSync {
            task?.cancel()
            task = null
            schedulers.remove(this)
        }
        return this
    }

    companion object {

        private val schedulers = hashSetOf<ScriptScheduler>()

        fun unregister() {
            callSync {
                for (scheduler in schedulers.toList()) {
                    scheduler.unregister()
                }
                schedulers.clear()
            }
        }

        @JvmStatic
        fun create() = ScriptScheduler()

    }

}
