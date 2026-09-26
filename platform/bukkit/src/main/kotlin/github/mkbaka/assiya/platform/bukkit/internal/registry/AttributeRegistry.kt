package github.mkbaka.assiya.platform.bukkit.internal.registry

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.AttributeParser
import github.mkbaka.assiya.common.parser.DefaultAttributeParser
import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.AttributeSourcePartParser
import github.mkbaka.assiya.common.parser.part.TextAttributeSourcePart
import github.mkbaka.assiya.common.parser.part.TextAttributeSourcePartParser
import github.mkbaka.assiya.common.parser.rule.TextRule
import github.mkbaka.assiya.common.parser.rule.TextRuleMatched
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import java.time.Duration

object AttributeRegistry : Registry<Attribute>("属性"), AttributeParser {

    @Volatile
    private var parser: AttributeParser = compile(emptyList(), emptyList())

    override fun parse(parts: List<AttributeSourcePart>): AttributeParsedSource {
        return parser.parse(parts)
    }

    fun register(attribute: Attribute) {
        register(attribute.id, attribute)
    }

    fun unregister(attribute: Attribute): Boolean {
        return unregister(attribute.id, attribute)
    }

    fun refresh() {
        parser = compile(
            attributes = values.toList(),
            conditions = ConditionRegistry.values.toList()
        )
    }

    fun compile(attributes: List<Attribute>, conditions: List<Condition>): AttributeParser {
        val attributeRules = attributes.mapNotNull(::compile)
        val conditionRules = conditions.mapNotNull(::compile)

        return DefaultAttributeParser(
            listOf(
                CachedSourcePartParser(
                    delegate = TextAttributeSourcePartParser(
                        attributeRules + conditionRules,
                    )
                )
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun compile(attribute: Attribute): TextRule? {
        val component = attribute[ReaderComponent] ?: return null
        val reader = ReaderRegistry[component.readerId] ?: return null

        return TextRule(
            names = component.names,
            capture = reader.capture,
            patterns = reader.patterns,
            target = TextRule.Target.Attribute(
                id = attribute.id,
                capture = reader.capture
            )
        )
    }

    private fun compile(condition: Condition): TextRule? {
        if (condition.names.isEmpty()) {
            return null
        }

        val reader = ReaderRegistry[condition.reader] ?: return null

        return TextRule(
            names = condition.names,
            capture = reader.capture,
            patterns = reader.patterns,
            target = TextRule.Target.Condition(condition.id),
        )
    }

    private class CachedSourcePartParser(
        private val delegate: TextAttributeSourcePartParser,
    ) : AttributeSourcePartParser {

        override val type: String = delegate.type

        private val cache: Cache<String, List<TextRuleMatched>> = Caffeine.newBuilder()
            .maximumSize(MAX_CACHED_LINES)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build()

        override fun parse(part: AttributeSourcePart): AttributeParsedSource {
            require(part is TextAttributeSourcePart) {
                "非文本类型无法由 ${this::class.java.simpleName} 解析"
            }
            val results = part.lines.map { line -> cache.get(line) { delegate.parseLine(line) }!! }
            return delegate.toSource(results)
        }

        private companion object {

            const val MAX_CACHED_LINES = 10_000L

        }

    }


}
