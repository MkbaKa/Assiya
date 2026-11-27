package github.mkbaka.assiya.platform.bukkit.internal.script.tools

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.GlobalDispatcher
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import org.bukkit.scheduler.BukkitRunnable

class ScriptScheduler {

    private var async = false
    private var delay = -1L
    private var period = -1L
    private lateinit var handler: Runnable

    private var cancelTask: Runnable? = null

    // Folia 触发后还会向后台排队, 注销或重新注册时使旧正文失效.
    // global 线程写入, 后台线程读取, 因此需要 volatile 可见性.
    @Volatile
    private var registration: Any? = null

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
        BukkitCoroutine.launch(GlobalDispatcher) {
            unregister()
            val token = Any()
            registration = token

            val registeredAsync = async
            val action = Runnable {
                if (registration !== token) return@Runnable
                handler.run()
            }

            if (FoliaCompat.isFolia()) {
                try {
                    cancelTask = FoliaCompat.scheduleGlobal(delay, period) {
                        if (registeredAsync) {
                            BukkitCoroutine.launch {
                                try {
                                    action.run()
                                } catch (cause: Throwable) {
                                    logEx(cause, "Failed to execute script scheduler")
                                }
                            }
                        } else {
                            action.run()
                        }
                    }
                    schedulers.add(this@ScriptScheduler)
                } catch (cause: Throwable) {
                    registration = null
                    throw cause
                }
            } else {
                val runnable = object : BukkitRunnable() {
                    override fun run() {
                        action.run()
                    }
                }
                val plugin = AssiyaBukkitPlugin.INSTANCE
                val task = when {
                    async && period > 0 -> runnable.runTaskTimerAsynchronously(plugin, delay.coerceAtLeast(0L), period)
                    async && delay > 0 -> runnable.runTaskLaterAsynchronously(plugin, delay)

                    period > 0 -> runnable.runTaskTimer(plugin, delay.coerceAtLeast(0L), period)
                    delay > 0 -> runnable.runTaskLater(plugin, delay)

                    async -> runnable.runTaskAsynchronously(plugin)
                    else -> runnable.runTask(plugin)
                }
                cancelTask = Runnable { task.cancel() }
                schedulers.add(this@ScriptScheduler)
            }
        }
        return this
    }

    fun unregister(): ScriptScheduler {
        BukkitCoroutine.launch(GlobalDispatcher) {
            cancelRegistration()
            schedulers.remove(this@ScriptScheduler)
        }
        return this
    }

    private fun cancelRegistration() {
        registration = null
        cancelTask?.run()
        cancelTask = null
    }

    companion object {

        private val schedulers = hashSetOf<ScriptScheduler>()

        fun unregister() {
            BukkitCoroutine.launch(GlobalDispatcher) {
                for (scheduler in schedulers.toList()) {
                    scheduler.unregister()
                }
                schedulers.clear()
            }
        }

        // 停服时 tick scheduler 已停止, 直接取消已有任务, 不再提交 global 调度.
        internal fun shutdown() {
            for (scheduler in schedulers) {
                scheduler.cancelRegistration()
            }
            schedulers.clear()
        }

        @JvmStatic
        fun create() = ScriptScheduler()

    }

}
