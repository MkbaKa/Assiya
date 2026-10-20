package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisherBase
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.common.source.mergeAttributeValues
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.feature.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.feature.BukkitAttributeEngine
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.*

object AssiyaAPI {

    @JvmStatic
    fun getSession(entity: Entity): AssiyaSession? = AssiyaSessionRegistry[entity.uniqueId]

    @JvmStatic
    fun getSession(uuid: UUID): AssiyaSession? = AssiyaSessionRegistry[uuid]

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
        return AttributeReadPipeline.evaluate(entity, parsed)
    }

    @JvmStatic
    fun parse(lines: List<String>): AttributeParsedSource = AttributeReadPipeline.parse(lines)

    @JvmStatic
    fun evaluate(
        entity: Entity,
        parsed: AttributeParsedSource
    ): Map<String, AttributeValue<*>> = AttributeReadPipeline.evaluate(entity, parsed)

    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        entity: Entity,
        sourceId: String,
        attributes: List<String>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val parsed = AttributeReadPipeline.parse(attributes)
        addAttribute(entity, sourceId, parsed, priority, lifeCycle, triggerEvent)
    }

    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        entity: Entity,
        sourceId: String,
        itemStack: ItemStack,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val parts = AttributeReadPipeline.extract(itemStack)
        val parsed = AttributeReadPipeline.parseParts(parts)
        addAttribute(entity, sourceId, parsed, priority, lifeCycle, triggerEvent)
    }

    @JvmStatic
    @JvmOverloads
    fun addAttribute(
        entity: Entity,
        sourceId: String,
        attributes: Map<String, AttributeValue<*>>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val session = getSession(entity) ?: return

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
        entity: Entity,
        sourceId: String,
        parsed: AttributeParsedSource,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val session = getSession(entity) ?: return

        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            AttributeReadPipeline.evaluate(entity, parsed)
        )

        session.attribute.submitPatches(triggerEvent, patch)
    }

    @JvmStatic
    @JvmOverloads
    fun addAttributeItems(
        entity: Entity,
        sourceId: String,
        items: List<ItemStack>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable,
        triggerEvent: Boolean = false
    ) {
        val session = getSession(entity) ?: return

        val values = items.flatMap { item ->
            val parts = AttributeReadPipeline.extract(item)

            val parsed = AttributeReadPipeline.parseParts(parts)

            val result = AttributeReadPipeline.evaluate(entity, parsed)

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

}
