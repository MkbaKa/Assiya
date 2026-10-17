package github.mkbaka.assiya.platform.bukkit.internal.equipment

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import github.mkbaka.assiya.platform.bukkit.internal.parser.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.delayTicks
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.launch
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.withAsync
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
                pending.remove(ownerId, task)
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

        // 把基于物品拿到的 part 塞进异步处理
        val parsedSources = withAsync {
            val sources = linkedMapOf<String, AttributeParsedSource>()
            for ((sourceId, parts) in inputs) {
                currentCoroutineContext().ensureActive()
                sources[sourceId] = AttributeReadPipeline.parseParts(parts)
            }
            sources
        }

        // 条件判断结果
        val conditionResults = linkedMapOf<String, BooleanArray>()
        for ((sourceId, parsed) in parsedSources) {
            currentCoroutineContext().ensureActive()
            conditionResults[sourceId] = parsed.evaluateConditions {
                ConditionRegistry.eval(entity, it)
            }
        }

        // 满足条件/无条件的可用属性结果
        withAsync {
            val results = arrayListOf<AttributeSourcePatch>()
            for ((sourceId, parsed) in parsedSources) {
                currentCoroutineContext().ensureActive()

                // 如果物品没有可解析的 part
                // 那么 resolve 的属性结果也会是一个空 Map
                // 搓个无内容 patch 覆盖掉旧装备给的属性
                results += AttributeSourcePatch(
                    sourceId = sourceId,
                    priority = priority,
                    lifeCycle = AttributeSourceLifecycle.Stable,
                    attributes = parsed.resolve(conditionResults.getValue(sourceId))
                )
            }
            currentCoroutineContext().ensureActive()
            if (AssiyaSessionRegistry[session.ownerUUID] !== session) return@withAsync

            session.attribute.submitPatches(true, *results.toTypedArray())
        }
    }
}
