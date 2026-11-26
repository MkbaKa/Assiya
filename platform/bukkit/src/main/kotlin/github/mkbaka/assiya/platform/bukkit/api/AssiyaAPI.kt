package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisherBase
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.common.source.mergeAttributeValues
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.handler.component.HealMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.VanillaAttributesComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.HealProcessor
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.api.session.AttributeSession
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import github.mkbaka.assiya.platform.bukkit.internal.feature.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.feature.BukkitAttributeEngine
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.future.asCompletableFuture
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object AssiyaAPI {

    /**
     * 根据属性 id 或读取名获取属性
     *
     * @param idOrName 属性 id 或读取名
     * @return 未找到时返回 null
     */
    @JvmStatic
    fun getAttribute(idOrName: String): Attribute? {
        return AttributeRegistry[idOrName] ?: AttributeRegistry.findByName(idOrName)
    }

    /**
     * 查询实体已有的 session
     *
     * @param entity 目标实体
     * @return 不存在时返回 null
     */
    @JvmStatic
    fun getSession(entity: Entity): AssiyaSession? = AssiyaSessionRegistry[entity.uniqueId]

    /**
     * 根据实体 UUID 查询已有的 session
     *
     * @param uuid 目标实体 UUID
     * @return 不存在时返回 null
     */
    @JvmStatic
    fun getSession(uuid: UUID): AssiyaSession? = AssiyaSessionRegistry[uuid]

    /**
     * 唤起一次装备属性刷新
     *
     * @param entity 需要重新读取装备的实体
     * @param changedSlots 需要更新的槽位
     */
    @JvmStatic
    fun refreshEquipment(
        entity: LivingEntity,
        vararg changedSlots: BukkitEquipmentSlot = BukkitEquipmentSlot.entries.toTypedArray()
    ) {
        EquipmentPipeline.refresh(entity, *changedSlots)
    }

    /**
     * 获取目标 session 不存在时创建
     *
     * 开启 [withAttribute] 时初始化尚未创建的 [AttributeSession] 并执行一次装备属性刷新
     *  已有 session 不会重复初始化
     *
     * @param entity 目标实体
     * @param withAttribute 是否初始化属性
     * @return 目标实体的 session
     */
    @JvmStatic
    @JvmOverloads
    fun getSessionOrCreate(entity: Entity, withAttribute: Boolean = true): AssiyaSession {
        val session = AssiyaSessionRegistry.getOrCreate(entity)

        if (withAttribute && !session.hasAttribute) {
            session.initAttribute(
                BukkitAttributeEngine(
                    session,
                    BukkitCoroutine,
                    AttributeEngineSnapshotPublisherBase()
                )
            )
        }

        return session
    }

    /**
     * 获取目标最后一次计算的属性结果
     *
     * @param entity 目标实体
     * @return 若实体没有 session 时返回 [AttributeEngineSnapshot.EMPTY].
     */
    @JvmStatic
    fun getSnapshot(entity: Entity): AttributeEngineSnapshot {
        return getSession(entity)?.attribute?.snapshot ?: AttributeEngineSnapshot.EMPTY
    }

    /**
     * 解析属性文本 并判断条件
     *
     * @param entity 条件判断目标
     * @param lines 待解析的属性文本
     * @return 解析结果
     */
    @JvmStatic
    fun parse(entity: Entity, lines: List<String>): Map<String, AttributeValue<*>> {
        return evaluate(entity, parse(lines), ConditionContextImpl())
    }

    /**
     * 解析属性文本 不判断条件
     *
     * @param lines 待解析的属性文本
     * @return 可供 [evaluate] 使用的解析结果
     */
    @JvmStatic
    fun parse(lines: List<String>): AttributeParsedSource = AttributeReadPipeline.parse(lines)

    /**
     * 对解析结果执行条件判断与属性值求值.
     *
     * @param entity 条件判断目标
     * @param parsed 已解析的属性来源
     * @param context 条件上下文
     * @return 解析结果
     */
    @JvmStatic
    fun evaluate(
        entity: Entity,
        parsed: AttributeParsedSource,
        context: ConditionContextImpl
    ): Map<String, AttributeValue<*>> =
        AttributeReadPipeline.evaluate(entity, parsed, context)

    /**
     * 增加属性
     *
     * @param session 目标
     * @param sourceId 属性源
     * @param attributes 待解析的属性文本
     * @param priority 来源优先级
     * @param lifeCycle 来源生命周期
     * @param triggerEvent 是否触发属性更新事件
     * @return 计算后的结果
     */
    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        session: AssiyaSession,
        sourceId: String,
        attributes: List<String>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val parsed = parse(attributes)
        addAttribute(session, sourceId, parsed, priority, lifeCycle, triggerEvent)
    }

    /**
     * 从物品中读取并增加属性
     *
     * @param session 目标
     * @param sourceId 属性源
     * @param itemStack 待读取的物品
     * @param priority 来源优先级
     * @param lifeCycle 来源生命周期
     * @param triggerEvent 是否触发属性更新事件
     * @param context 条件上下文
     * @return 计算后的结果
     */
    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        session: AssiyaSession,
        sourceId: String,
        itemStack: ItemStack,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false,
        context: ConditionContextImpl = ConditionContextImpl(itemStack = itemStack, sourceId = sourceId)
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val parts = AttributeReadPipeline.readItem(itemStack)
        val parsed = AttributeReadPipeline.parseParts(parts)
        addAttribute(session, sourceId, parsed, priority, lifeCycle, triggerEvent, context)
    }

    /**
     * 增加已解析的属性
     *
     * @param session 目标
     * @param sourceId 属性源
     * @param attributes 已解析的属性值
     * @param priority 来源优先级
     * @param lifeCycle 来源生命周期
     * @param triggerEvent 是否触发属性更新事件
     * @return 计算后的结果
     */
    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        session: AssiyaSession,
        sourceId: String,
        attributes: Map<String, AttributeValue<*>>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            attributes
        )

        addAttribute(session, triggerEvent = triggerEvent, patch)
    }

    /**
     * 判断解析结果中的条件并增加属性
     *
     * @param session 目标
     * @param sourceId 属性源
     * @param parsed 已解析的属性来源
     * @param priority 来源优先级
     * @param lifeCycle 来源生命周期
     * @param triggerEvent 是否触发属性更新事件
     * @param context 条件上下文
     * @return 计算后的结果
     */
    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        session: AssiyaSession,
        sourceId: String,
        parsed: AttributeParsedSource,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false,
        context: ConditionContextImpl = ConditionContextImpl(sourceId = sourceId)
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val entity = session.owner
            ?: return@attributeOperation CompletableFuture.completedFuture(session.attribute.snapshot)

        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            AttributeReadPipeline.evaluate(entity, parsed, context)
        )

        addAttribute(session, triggerEvent = triggerEvent, patch)
    }

    /**
     * 增加属性 不触发属性更新事件
     *
     * @param session 目标
     * @param patch 待增加的属性源
     * @return 计算后的结果
     */
    @JvmStatic
    fun addAttribute(
        session: AssiyaSession,
        vararg patch: AttributeSourcePatch,
    ): CompletableFuture<AttributeEngineSnapshot> = addAttribute(session, false, *patch)

    /**
     * 增加属性
     *
     * @param session 目标
     * @param triggerEvent 是否触发属性更新事件
     * @param patch 待增加的属性源
     * @return 计算后的结果
     */
    @JvmStatic
    fun addAttribute(
        session: AssiyaSession,
        triggerEvent: Boolean,
        vararg patch: AttributeSourcePatch,
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val reply = CompletableDeferred<AttributeEngineSnapshot>()

        val actions: List<AttributeEngineAction> =
            patch.mapTo(ArrayList()) {
                AttributeEngineAction.UpdateSource(it)
            } + AttributeEngineAction.Refresh(triggerEvent, reply)

        session.attribute.submit(*actions.toTypedArray())

        reply.asCompletableFuture()
    }

    /**
     * 从多个物品中读取属性 合并为同一个属性源
     *
     * @param session 目标
     * @param sourceId 属性源
     * @param items 待读取的物品
     * @param priority 来源优先级
     * @param lifeCycle 来源生命周期
     * @param triggerEvent 是否触发属性更新事件
     * @return 计算后的结果
     */
    @JvmStatic
    @JvmOverloads
    fun addAttributeItems(
        session: AssiyaSession,
        sourceId: String,
        items: List<ItemStack>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val entity = session.owner
            ?: return@attributeOperation CompletableFuture.completedFuture(session.attribute.snapshot)

        val values = items.flatMap { item ->
            val parts = AttributeReadPipeline.readItem(item)

            val parsed = AttributeReadPipeline.parseParts(parts)

            val context = ConditionContextImpl(itemStack = item, sourceId = sourceId)
            val result = AttributeReadPipeline.evaluate(entity, parsed, context)

            result.values
        }

        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            mergeAttributeValues(values)
        )

        addAttribute(session, triggerEvent = triggerEvent, patch)
    }

    /**
     * 增加快照中的属性
     *
     * 保留快照中的属性源和优先级 统一使用指定的生命周期
     *  不影响目标的其他属性源 也不会触发属性更新事件
     *
     * @param session 目标
     * @param snapshot 属性快照
     * @param lifeCycle 来源生命周期
     * @param triggerEvent 是否触发属性更新事件
     * @return 计算后的结果
     */
    @JvmStatic
    @JvmOverloads
    fun addAttributeSnapshot(
        session: AssiyaSession,
        snapshot: AttributeEngineSnapshot,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ): CompletableFuture<AttributeEngineSnapshot> = attributeOperation {
        val patches = snapshot.sources.map { (sourceId, source) ->
            AttributeSourcePatch(
                sourceId = sourceId,
                priority = source.priority,
                lifeCycle = lifeCycle,
                attributes = source.attributes
            )
        }

        addAttribute(session, triggerEvent = triggerEvent, *patches.toTypedArray())
    }

    /**
     * 移除指定属性源
     *
     * @param entity 目标实体
     * @param sourceId 属性源
     * @return 计算后的结果 若实体没有 session 时返回 [AttributeEngineSnapshot.EMPTY]
     */
    @JvmStatic
    fun takeAttribute(entity: Entity, sourceId: String): CompletableFuture<AttributeEngineSnapshot> =
        attributeOperation {
            val session = getSession(entity)
                ?: return@attributeOperation CompletableFuture.completedFuture(AttributeEngineSnapshot.EMPTY)
            takeAttribute(session, sourceId)
        }

    /**
     * 移除指定属性源
     *
     * @param session 目标
     * @param sourceId 属性源
     * @return 计算后的结果
     */
    @JvmStatic
    fun takeAttribute(session: AssiyaSession, sourceId: String): CompletableFuture<AttributeEngineSnapshot> =
        attributeOperation {
            val reply = CompletableDeferred<AttributeEngineSnapshot>()
            session.attribute.submit(
                AttributeEngineAction.RemoveSource(sourceId),
                AttributeEngineAction.Refresh(triggerEvent = false, reply = reply)
            )
            reply.asCompletableFuture()
        }

    /**
     * 见 [DamageMeta.fixedDamage]
     *
     * @param attacker 攻击者
     * @param defender 受击者
     * @param damage 伤害值
     */
    @JvmStatic
    fun attackTo(attacker: Entity, defender: LivingEntity, damage: Double) {
        DamageMeta.fixedDamage(attacker, defender, damage).doDamage()
    }

    /**
     * 判断目标是否正在执行 Assiya 伤害
     *
     * @param attacker 攻击者
     * @return 正在执行时返回 true
     */
    @JvmStatic
    fun isDamaging(attacker: Entity) = getSession(attacker)?.damageMetaStack?.current != null

    @JvmStatic
    @JvmOverloads
    fun heal(
        entity: LivingEntity,
        value: Double,
        builder: Consumer<ProcessorComponentBuilder>? = null
    ) {
        val vanilla = VanillaAttributes.of(entity)
        val processor = HealProcessor.of(entity, value) { pcb ->
            pcb += VanillaAttributesComponent(vanilla)
            builder?.accept(pcb)
        }

        processor.process()

        val totalHeal = processor[HealMultipleComponent.Key]?.totalHeal ?: return

        entity.health = (entity.health + totalHeal).coerceAtMost(vanilla.maxHealth.value)
    }

    private inline fun attributeOperation(
        operation: () -> CompletableFuture<AttributeEngineSnapshot>
    ): CompletableFuture<AttributeEngineSnapshot> {
        val future = CompletableFuture<AttributeEngineSnapshot>()

        try {
            operation().whenComplete { snapshot, cause ->
                if (cause == null) {
                    future.complete(snapshot)
                } else {
                    future.completeExceptionally(cause)
                }
            }
        } catch (cause: Exception) {
            future.completeExceptionally(cause)
        }

        return future
    }

}
