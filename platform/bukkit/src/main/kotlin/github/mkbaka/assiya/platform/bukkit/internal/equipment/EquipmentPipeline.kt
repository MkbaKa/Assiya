package github.mkbaka.assiya.platform.bukkit.internal.equipment

import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import github.mkbaka.assiya.platform.bukkit.internal.feature.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.delayTicks
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.launch
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.withAsync
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import kotlinx.coroutines.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*

object EquipmentPipeline {

    private val pending = hashMapOf<UUID, Job>()

    fun cancel(ownerId: UUID) {
        pending.remove(ownerId)?.cancel()
    }

    fun refresh(
        entity: LivingEntity,
        vararg changedSlots: BukkitEquipmentSlot = BukkitEquipmentSlot.entries.toTypedArray()
    ) {
        refresh(entity, Config.global.EQUIPMENT_REFRESH_DELAY_TICKS, *changedSlots)
    }

    fun refresh(
        entity: LivingEntity,
        refreshDelay: Long,
        vararg changedSlots: BukkitEquipmentSlot
    ) {
        val ownerId = entity.uniqueId
        val session = AssiyaSessionRegistry[ownerId] ?: return
        if (!session.hasAttribute) return

        // 追加受影响的槽位
        session.dirtyEquipmentSlots.addAll(changedSlots)
        pending.remove(ownerId)?.cancel()

        val sync = BukkitCoroutine.coroutineContext[BukkitDispatchers]!!.sync

        val job = launch(
            context = sync,
            start = CoroutineStart.LAZY
        ) {
            val task = currentCoroutineContext().job

            try {
                if (refreshDelay > 0) delayTicks(refreshDelay)

                if (AssiyaSessionRegistry[ownerId] !== session || entity is Player && !entity.isOnline) {
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
        val priorities = Config.global.EQUIPMENT_SOURCE_PRIORITY
        val changedSlots = session.dirtyEquipmentSlots.toHashSet()
        val equipment = entity.equipment
        val items = BukkitEquipmentSlot.entries.associateWith { slot ->
            slot.getSlotItem(equipment)
        }

        // 重新解析变动槽位里的新物品
        val updates = withAsync {
            changedSlots.associateWith { slot ->
                val parts = AttributeReadPipeline.readItem(items.getValue(slot))
                AttributeReadPipeline.parseParts(parts)
            }
        }

        currentCoroutineContext().ensureActive()
        if (AssiyaSessionRegistry[session.ownerUUID] !== session || entity is Player && !entity.isOnline) return

        // 塞进缓存
        session.parsedEquipment.putAll(updates)
        // 更新缓存之后再清理变动的槽位
        // 这样频繁触发的事件会一直保留到最后一次解析成功
        session.dirtyEquipmentSlots.removeAll(changedSlots)

        val parsedSources = BukkitEquipmentSlot.entries.associate { slot ->
            slot.sourceId to session.parsedEquipment.getValue(slot)
        }

        // 条件判断结果
        val conditionResults = linkedMapOf<String, BooleanArray>()
        for ((slot, item) in items) {
            val sourceId = slot.sourceId
            val parsed = parsedSources.getValue(sourceId)
            val context = ConditionContextImpl(equipmentSlot = slot, itemStack = item, sourceId = sourceId)
            currentCoroutineContext().ensureActive()
            conditionResults[sourceId] = parsed.evaluateConditions {
                ConditionRegistry.eval(entity, it, context)
            }
        }

        // 满足条件/无条件的可用属性结果
        withAsync {
            val results = arrayListOf<AttributeSourcePatch>()
            for (slot in BukkitEquipmentSlot.entries) {
                val sourceId = slot.sourceId
                val parsed = parsedSources.getValue(sourceId)
                currentCoroutineContext().ensureActive()

                // 如果物品没有可解析的 part
                // 那么 resolve 的属性结果也会是一个空 Map
                // 搓个无内容 patch 覆盖掉旧装备给的属性
                results += AttributeSourcePatch(
                    sourceId = sourceId,
                    priority = priorities.getValue(slot),
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
