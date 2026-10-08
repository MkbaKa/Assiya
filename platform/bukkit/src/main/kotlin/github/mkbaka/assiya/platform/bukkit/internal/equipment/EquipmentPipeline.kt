package github.mkbaka.assiya.platform.bukkit.internal.equipment

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import github.mkbaka.assiya.platform.bukkit.internal.parser.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.delayTicks
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.launch
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import kotlinx.coroutines.*
import org.bukkit.entity.LivingEntity
import java.util.*

object EquipmentPipeline {

    private val pending = hashMapOf<UUID, Job>()

    fun cancel(ownerId: UUID) {
        pending.remove(ownerId)?.cancel()
    }

    fun refresh(entity: LivingEntity, refreshDelay: Long = Config.global.EQUIPMENT_REFRESH_DELAY_TICKS) {
        refresh0(entity, refreshDelay)
    }

    private fun refresh0(entity: LivingEntity, delay: Long) {
        val ownerId = entity.uniqueId
        val session = AssiyaSessionRegistry[ownerId] ?: return

        pending.remove(ownerId)?.cancel()

        val sync = BukkitCoroutine.coroutineContext[BukkitDispatchers]!!.sync

        val job = launch(
            context = sync,
            start = CoroutineStart.LAZY
        ) {
            val task = currentCoroutineContext().job

            try {
                delayTicks(delay)

                if (AssiyaSessionRegistry[ownerId] !== session) {
                    return@launch
                }

                readAndSubmit(entity, session)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logEx(e, "Failed to refresh equipment for $ownerId")
            } finally {
                if (pending[ownerId] === task) {
                    pending.remove(ownerId)
                }
            }
        }

        pending[ownerId] = job
        job.start()
    }

    private suspend fun readAndSubmit(
        entity: LivingEntity,
        session: AssiyaSession
    ) {
        val priority = Config.global.EQUIPMENT_SOURCE_PRIORITY
        val equipment = entity.equipment
        val inputs = BukkitEquipmentSlot.entries.associate { slot ->
            slot.sourceId to AttributeReadPipeline.extract(
                slot.getSlotItem(equipment)
            )
        }

        val parsedSources =
            linkedMapOf<String, AttributeParsedSource>()
        val patches = arrayListOf<AttributeSourcePatch>()

        for ((sourceId, parts) in inputs) {
            currentCoroutineContext().ensureActive()

            if (parts.isEmpty()) {
                patches += AttributeSourcePatch(
                    sourceId = sourceId,
                    priority = priority,
                    lifeCycle = AttributeSourceLifecycle.Stable,
                    attributes = emptyList()
                )
                continue
            }

            val parsed = AttributeReadPipeline.parseParts(parts)
            parsedSources[sourceId] = parsed

            patches += AttributeReadPipeline.patch(
                entity = entity,
                sourceId = sourceId,
                parsed = parsed,
                priority = priority,
                lifeCycle = AttributeSourceLifecycle.Stable
            )
        }

        currentCoroutineContext().ensureActive()

        if (AssiyaSessionRegistry[session.ownerUUID] !== session) {
            return
        }

        session.attribute.submitPatches(true, *patches.toTypedArray())
    }
}
