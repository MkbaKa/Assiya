package github.mkbaka.assiya.platform.bukkit.internal.parser

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.TextAttributeSourcePart
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSession
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.withAsync
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.withSync
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.uncolored
import github.mkbaka.assiya.platform.bukkit.internal.util.isAir
import kotlinx.coroutines.withContext
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

internal object AttributeReadPipeline {

    fun text(lines: List<String>): TextAttributeSourcePart {
        return TextAttributeSourcePart(lines.map { it.uncolored() })
    }

    fun extract(item: ItemStack?): List<AttributeSourcePart> {
        if (item.isAir) return emptyList()

        val lore = item.itemMeta?.lore.orEmpty()
        if (lore.isEmpty()) return emptyList()

        return listOf(text(lore))
    }

    fun parseSync(
        parts: List<AttributeSourcePart>
    ): AttributeParsedSource = AttributeRegistry.parse(parts)

    suspend fun parse(
        parts: List<AttributeSourcePart>
    ): AttributeParsedSource = withAsync {
        parseSync(parts)
    }

    suspend fun parse(item: ItemStack): AttributeParsedSource {
        val parts = withSync { extract(item) }
        return parse(parts)
    }

    fun evaluateSync(
        entity: Entity,
        parsed: AttributeParsedSource
    ): List<AttributeValue<*>> {
        val conditions = parsed.evaluateConditions {
            ConditionRegistry.eval(entity, it)
        }
        return parsed.resolve(conditions)
    }

    suspend fun evaluate(
        entity: Entity,
        parsed: AttributeParsedSource
    ): List<AttributeValue<*>> {
        val conditions = withSync {
            parsed.evaluateConditions {
                ConditionRegistry.eval(entity, it)
            }
        }
        return withAsync {
            parsed.resolve(conditions)
        }
    }

    suspend fun patch(
        entity: Entity,
        sourceId: String,
        parsed: AttributeParsedSource,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ): AttributeSourcePatch = AttributeSourcePatch(
        sourceId,
        priority,
        lifeCycle,
        evaluate(entity, parsed)
    )

    suspend fun submit(
        session: AttributeSession,
        entity: Entity,
        sourceId: String,
        parsed: AttributeParsedSource,
        priority: Int,
        lifeCycle: AttributeSourceLifecycle
    ) {
        val patch = patch(
            entity, sourceId, parsed, priority, lifeCycle
        )
        session.submitPatches(listOf(patch))
    }

}