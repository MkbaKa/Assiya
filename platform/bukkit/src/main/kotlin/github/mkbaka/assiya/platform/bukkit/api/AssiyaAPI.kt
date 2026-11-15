package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisherBase
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.common.source.mergeAttributeValues
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.feature.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.feature.BukkitAttributeEngine
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.*

object AssiyaAPI {

    @JvmStatic
    fun getAttribute(idOrName: String): Attribute? {
        return AttributeRegistry[idOrName] ?: AttributeRegistry.findByName(idOrName)
    }

    @JvmStatic
    fun getSession(entity: Entity): AssiyaSession? = AssiyaSessionRegistry[entity.uniqueId]

    @JvmStatic
    fun getSession(uuid: UUID): AssiyaSession? = AssiyaSessionRegistry[uuid]

    @JvmStatic
    fun refreshEquipment(entity: LivingEntity) {
        EquipmentPipeline.refresh(entity)
    }

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

    @JvmStatic
    fun getSnapshot(entity: Entity): AttributeEngineSnapshot {
        return getSession(entity)?.attribute?.snapshot ?: AttributeEngineSnapshot.EMPTY
    }

    @JvmStatic
    fun parse(entity: Entity, lines: List<String>): Map<String, AttributeValue<*>> {
        val parsed = AttributeReadPipeline.parse(lines)
        return AttributeReadPipeline.evaluate(entity, parsed, ConditionContextImpl())
    }

    @JvmStatic
    fun parse(lines: List<String>): AttributeParsedSource = AttributeReadPipeline.parse(lines)

    @JvmStatic
    fun evaluate(
        entity: Entity,
        parsed: AttributeParsedSource,
        context: ConditionContextImpl
    ): Map<String, AttributeValue<*>> =
        AttributeReadPipeline.evaluate(entity, parsed, context)

    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        session: AssiyaSession,
        sourceId: String,
        attributes: List<String>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val parsed = AttributeReadPipeline.parse(attributes)
        addAttribute(session, sourceId, parsed, priority, lifeCycle, triggerEvent)
    }

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
    ) {
        val parts = AttributeReadPipeline.readItem(itemStack)
        val parsed = AttributeReadPipeline.parseParts(parts)
        addAttribute(session, sourceId, parsed, priority, lifeCycle, triggerEvent, context)
    }

    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        session: AssiyaSession,
        sourceId: String,
        attributes: Map<String, AttributeValue<*>>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            attributes
        )

        session.attribute.submitPatches(triggerEvent, patch)
    }

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
    ) {
        val entity = session.owner ?: return

        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            AttributeReadPipeline.evaluate(entity, parsed, context)
        )

        session.attribute.submitPatches(triggerEvent, patch)
    }

    @JvmStatic
    @JvmOverloads
    fun addAttributeItems(
        session: AssiyaSession,
        sourceId: String,
        items: List<ItemStack>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val entity = session.owner ?: return

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

        session.attribute.submitPatches(triggerEvent, patch)
    }

    @JvmStatic
    fun takeAttribute(entity: Entity, sourceId: String) {
        val session = getSession(entity) ?: return
        session.attribute.submitRemove(sourceId)
    }

    @JvmStatic
    fun attackTo(attacker: Entity, defender: LivingEntity, damage: Double) {
        DamageMeta.fixedDamage(attacker, defender, damage).doDamage()
    }

    @JvmStatic
    fun isDamaging(attacker: Entity) = getSession(attacker)?.damageMetaStack?.current != null

}
