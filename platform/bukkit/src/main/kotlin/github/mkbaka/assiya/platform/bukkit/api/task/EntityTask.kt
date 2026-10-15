package github.mkbaka.assiya.platform.bukkit.api.task

import github.mkbaka.assiya.platform.bukkit.internal.registry.EntityTaskRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import kotlinx.coroutines.Job
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class EntityTask(
    val ownerId: UUID,
    val taskId: String,
    val job: Job,
    val key: String?,
    private val onTerminate: Runnable
) {

    private val finished = AtomicBoolean()

    fun cancel() {
        stop(true)
    }

    internal fun replace() {
        stop(false)
    }

    private fun stop(terminate: Boolean) {
        val needsCleanup = terminate && (!job.isCompleted || job.isCancelled)
        finish(needsCleanup, cancel = true)
    }

    internal fun finish(terminate: Boolean, cancel: Boolean = false) {
        if (!finished.compareAndSet(false, true)) return
        EntityTaskRegistry.remove(ownerId, this)
        if (cancel) job.cancel()
        if (!terminate) return
        try {
            onTerminate.run()
        } catch (e: Throwable) {
            logEx(e, "Failed to terminate entity task '$taskId' for $ownerId")
        }
    }

}