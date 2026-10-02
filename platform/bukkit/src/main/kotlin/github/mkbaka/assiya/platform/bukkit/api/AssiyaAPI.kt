package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.parser.AttributeReadPipeline
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSession
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSessionRegistry.sessions
import kotlinx.coroutines.future.future
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * TODO refactor
 */
object AssiyaAPI {

    @JvmStatic
    fun getSession(entity: Entity): AttributeSession? = sessions[entity.uniqueId]

    @JvmStatic
    fun getSession(uuid: UUID): AttributeSession? = sessions[uuid]

    @JvmStatic
    fun getSessionOrCreate(entity: Entity): AttributeSession {
        return AttributeSessionRegistry.getOrCreate(entity)
    }

    @JvmStatic
    fun getSnapshot(entity: Entity): AttributeEngineSnapshot {
        return getSession(entity)?.snapshot ?: AttributeEngineSnapshot.EMPTY
    }

    @JvmStatic
    fun parseSync(entity: Entity, lines: List<String>): List<AttributeValue<*>> {
        val parsed = AttributeReadPipeline.parseSync(
            listOf(AttributeReadPipeline.text(lines))
        )

        return AttributeReadPipeline.evaluateSync(entity, parsed)
    }





    @JvmStatic
    fun parseSync(lines: List<String>): AttributeParsedSource =
        AttributeReadPipeline.parseSync(
            listOf(AttributeReadPipeline.text(lines))
        )

    @JvmStatic
    fun parseSync(item: ItemStack): AttributeParsedSource =
        AttributeReadPipeline.parseSync(
            AttributeReadPipeline.extract(item)
        )

    @JvmStatic
    fun parseAsync(
        lines: List<String>
    ): CompletableFuture<AttributeParsedSource> = BukkitCoroutine.future {
        AttributeReadPipeline.parse(
            listOf(AttributeReadPipeline.text(lines))
        )
    }

    @JvmStatic
    fun parseAsync(
        item: ItemStack
    ): CompletableFuture<AttributeParsedSource> = BukkitCoroutine.future {
        AttributeReadPipeline.parse(item)
    }

    @JvmStatic
    fun evaluateSync(
        entity: Entity,
        parsed: AttributeParsedSource
    ): List<AttributeValue<*>> =
        AttributeReadPipeline.evaluateSync(entity, parsed)

    @JvmStatic
    fun evaluateAsync(
        entity: Entity,
        parsed: AttributeParsedSource
    ): CompletableFuture<List<AttributeValue<*>>> = BukkitCoroutine.future {
        AttributeReadPipeline.evaluate(entity, parsed)
    }

    @JvmStatic
    fun submitAsync(
        entity: Entity,
        sourceId: String,
        parsed: AttributeParsedSource,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ): CompletableFuture<Unit> = BukkitCoroutine.future {
        AttributeReadPipeline.submit(
            AttributeSessionRegistry[entity]!!,
            entity, sourceId, parsed, priority, lifeCycle
        )
    }

    @JvmStatic
    fun parseAndSubmitAsync(
        entity: Entity,
        sourceId: String,
        lines: List<String>,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ): CompletableFuture<Unit> = BukkitCoroutine.future {
        val session = AttributeSessionRegistry[entity]!!
        val parsed = AttributeReadPipeline.parse(
            listOf(AttributeReadPipeline.text(lines))
        )
        AttributeReadPipeline.submit(
            session, entity, sourceId, parsed, priority, lifeCycle
        )
    }

    @JvmStatic
    fun parseAndSubmitAsync(
        entity: Entity,
        sourceId: String,
        item: ItemStack,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ): CompletableFuture<Unit> = BukkitCoroutine.future {
        val session = AttributeSessionRegistry[entity]!!
        val parsed = AttributeReadPipeline.parse(item)
        AttributeReadPipeline.submit(
            session, entity, sourceId, parsed, priority, lifeCycle
        )
    }

}
