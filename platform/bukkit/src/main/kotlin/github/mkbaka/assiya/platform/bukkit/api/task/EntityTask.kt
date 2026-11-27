package github.mkbaka.assiya.platform.bukkit.api.task

import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import github.mkbaka.assiya.platform.bukkit.internal.registry.EntityTaskRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import kotlinx.coroutines.Job
import org.bukkit.entity.Entity
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

class EntityTask internal constructor(
    internal val owner: Entity,
    val taskId: String,
    val job: Job,
    val key: String?,
    private val onTerminate: Consumer<Entity>
) {

    val ownerId: UUID = owner.uniqueId

    private val finished = AtomicBoolean()

    private val cleaned = AtomicBoolean()

    fun cancel() {
        finish(!job.isCompleted || job.isCancelled, cancel = true)
    }

    internal fun replace() {
        finish(false, cancel = true)
    }

    internal fun finish(terminate: Boolean, cancel: Boolean = false) {
        if (!finished.compareAndSet(false, true)) return
        if (terminate) EntityTaskRegistry.addCleanup(this)
        EntityTaskRegistry.remove(ownerId, this)
        if (cancel) job.cancel()
        if (!terminate) return
        if (FoliaCompat.isFolia() && !EntityTaskRegistry.isClosing) {
            BukkitCoroutine.launch(owner) { cleanup() }
        } else {
            cleanup()
        }
    }

    internal fun cleanup() {
        if (!cleaned.compareAndSet(false, true)) return
        try {
            onTerminate.accept(owner)
        } catch (cause: Throwable) {
            logEx(cause, "Failed to terminate entity task '$taskId' for $ownerId")
        } finally {
            EntityTaskRegistry.removeCleanup(this)
        }
    }

}
