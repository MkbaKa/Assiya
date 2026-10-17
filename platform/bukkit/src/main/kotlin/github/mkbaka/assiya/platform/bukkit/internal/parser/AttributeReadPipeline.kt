package github.mkbaka.assiya.platform.bukkit.internal.parser

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.TextAttributeSourcePart
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ItemReaderRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.withAsync
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.withSync
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.uncolored
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object AttributeReadPipeline {

    fun extract(item: ItemStack?): List<AttributeSourcePart> {
        return ItemReaderRegistry.read(item)
    }

    fun parseParts(
        parts: List<AttributeSourcePart>
    ): AttributeParsedSource = AttributeRegistry.parse(parts)

    fun parseSync(attributes: List<String>): AttributeParsedSource {
        return AttributeRegistry.parse(listOf(TextAttributeSourcePart(attributes.uncolored())))
    }

//    suspend fun parseParts(
//        parts: List<AttributeSourcePart>
//    ): AttributeParsedSource = withAsync { parsePartsSync(parts) }

//    suspend fun parse(
//        attributes: List<String>
//    ): AttributeParsedSource = withAsync { parseSync(attributes) }

//    suspend fun parse(item: ItemStack): AttributeParsedSource {
//        val parts = withSync { extract(item) }
//        return parse(parts)
//    }

    /**
     * TODO 设计不足
     *      parse 后的结果一定要 evaluate 才能变成可提交的 value
     */
    fun evaluateSync(
        entity: Entity,
        parsed: AttributeParsedSource
    ): Map<String, AttributeValue<*>> {
        val conditions = parsed.evaluateConditions {
            ConditionRegistry.eval(entity, it)
        }
        return parsed.resolve(conditions)
    }

    suspend fun evaluate(
        entity: Entity,
        parsed: AttributeParsedSource
    ): Map<String, AttributeValue<*>> {
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

}
