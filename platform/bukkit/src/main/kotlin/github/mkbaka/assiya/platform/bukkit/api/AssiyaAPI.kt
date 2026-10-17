package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisherBase
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.parser.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.session.BukkitAttributeEngine
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.*

/**
 * TODO refactor
 */
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
    fun parseSync(entity: Entity, lines: List<String>): List<AttributeValue<*>> {
        val parsed = AttributeReadPipeline.parseSync(lines)

        return AttributeReadPipeline.evaluateSync(entity, parsed)
    }

    // TODO triggerEvent

    @JvmStatic
    fun addAttribute(
        entity: Entity,
        sourceId: String,
        attributes: List<String>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ) {
        val session = getSession(entity) ?: return

        val parsed = AttributeReadPipeline.parseSync(attributes)
        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            AttributeReadPipeline.evaluateSync(entity, parsed)
        )

        addAttribute(session, patch)
    }

    @JvmStatic
    fun addAttribute(
        entity: Entity,
        sourceId: String,
        parsed: AttributeParsedSource,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ) {
        val session = getSession(entity) ?: return

        val patch = AttributeSourcePatch(
            sourceId,
            priority,
            lifeCycle,
            AttributeReadPipeline.evaluateSync(entity, parsed)
        )

        addAttribute(session, patch)
    }

    @JvmStatic
    fun addAttribute(
        entity: Entity,
        vararg patches: AttributeSourcePatch
    ) {
        val session = getSession(entity) ?: return
        addAttribute(session, *patches)
    }

    @JvmStatic
    fun addAttribute(
        session: AssiyaSession,
        vararg patches: AttributeSourcePatch
    ) {
        session.attribute.submitPatches(false, *patches)
    }

    @JvmStatic
    fun takeAttribute(entity: Entity, sourceId: String) {
        val session = getSession(entity) ?: return
        session.attribute.submitRemove(sourceId)
    }

    @JvmStatic
    fun parseSync(lines: List<String>): AttributeParsedSource =
        AttributeReadPipeline.parseSync(lines)


    @JvmStatic
    fun attackTo(attacker: Entity, defender: LivingEntity, damage: Double) {
        DamageMeta.fixedDamage(attacker, defender, damage).doDamage()
    }




    //    @JvmStatic
//    fun parseSync(item: ItemStack): AttributeParsedSource =
//        AttributeReadPipeline.parseSync(
//            AttributeReadPipeline.extract(item)
//        )
//
//    @JvmStatic
//    fun parseAsync(
//        lines: List<String>
//    ): CompletableFuture<AttributeParsedSource> = BukkitCoroutine.future {
//        AttributeReadPipeline.parse(lines)
//    }
//
//    @JvmStatic
//    fun parseAsync(
//        item: ItemStack
//    ): CompletableFuture<AttributeParsedSource> = BukkitCoroutine.future {
//        AttributeReadPipeline.parse(item)
//    }
//
    @JvmStatic
    fun evaluateSync(
        entity: Entity,
        parsed: AttributeParsedSource
    ): List<AttributeValue<*>> =
        AttributeReadPipeline.evaluateSync(entity, parsed)
//
//    @JvmStatic
//    fun evaluateAsync(
//        entity: Entity,
//        parsed: AttributeParsedSource
//    ): CompletableFuture<List<AttributeValue<*>>> = BukkitCoroutine.future {
//        AttributeReadPipeline.evaluate(entity, parsed)
//    }
//
//    @JvmStatic
//    fun submitAsync(
//        entity: Entity,
//        sourceId: String,
//        parsed: AttributeParsedSource,
//        priority: Int,
//        lifeCycle: AttributeSourceLifecycle
//    ): CompletableFuture<Unit> = BukkitCoroutine.future {
//        AttributeReadPipeline.submit(
//            AttributeSessionRegistry[entity]!!,
//            entity, sourceId, parsed, priority, lifeCycle
//        )
//    }
//
//    @JvmStatic
//    fun parseAndSubmitAsync(
//        entity: Entity,
//        sourceId: String,
//        lines: List<String>,
//        priority: Int,
//        lifeCycle: AttributeSourceLifecycle
//    ): CompletableFuture<Unit> = BukkitCoroutine.future {
//        val session = AttributeSessionRegistry[entity]!!
//        val parsed = AttributeReadPipeline.parse(lines)
//        AttributeReadPipeline.submit(
//            session, entity, sourceId, parsed, priority, lifeCycle
//        )
//    }
//
//    @JvmStatic
//    fun parseAndSubmitAsync(
//        entity: Entity,
//        sourceId: String,
//        item: ItemStack,
//        priority: Int,
//        lifeCycle: AttributeSourceLifecycle
//    ): CompletableFuture<Unit> = BukkitCoroutine.future {
//        val session = AttributeSessionRegistry[entity]!!
//        val parsed = AttributeReadPipeline.parse(item)
//        AttributeReadPipeline.submit(
//            session, entity, sourceId, parsed, priority, lifeCycle
//        )
//    }

}
