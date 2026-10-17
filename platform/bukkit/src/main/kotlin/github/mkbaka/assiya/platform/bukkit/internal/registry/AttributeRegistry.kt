package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.parser.*
import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.AttributeSourcePartParser
import github.mkbaka.assiya.common.parser.part.AttributeSourcePartParserFactory
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.internal.parser.MapParserFactory
import github.mkbaka.assiya.platform.bukkit.internal.parser.TextParserFactory

object AttributeRegistry : NamedRegistry<Attribute>("属性"), AttributeParser {

    private val parserFactories: List<AttributeSourcePartParserFactory> = listOf(
        TextParserFactory,
        MapParserFactory
    )

    @Volatile
    private var parser: AttributeParser = compile(emptyList(), emptyList())

    @Volatile
    private var attributesByName: Map<String, Attribute> = emptyMap()

    fun findByName(name: String): Attribute? = attributesByName[name]

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
        val attributes = values.toList()
        val nextParser = compile(
            attributes = attributes,
            conditions = ConditionRegistry.values.toList()
        )
        val names = hashMapOf<String, Attribute>()
        for (attribute in attributes) {
            val reader = attribute[ReaderComponent] ?: continue
            for (name in reader.names) names.putIfAbsent(name, attribute)
        }
        attributesByName = names
        parser = nextParser
    }

    fun compile(attributes: List<Attribute>, conditions: List<Condition>): AttributeParser {
        val rules = collectRules(attributes, conditions)

        val parsers = arrayListOf<AttributeSourcePartParser>()
        for (factory in parserFactories) {
            parsers += factory.create(rules)
        }

        return DefaultAttributeParser(parsers)
    }

    private fun collectRules(attributes: List<Attribute>, conditions: List<Condition>): RuleSet {
        val attributeRules = arrayListOf<Rule>()
        val conditionRules = arrayListOf<Rule>()
        for (attribute in attributes) {
            val component = attribute[ReaderComponent] ?: continue
            val reader = ReaderRegistry[component.readerId] ?: continue
            attributeRules += Rule(
                id = attribute.id,
                capture = reader.capture,
                names = component.names,
                patterns = reader.patterns
            )
        }
        for (condition in conditions) {
            val reader = ReaderRegistry[condition.reader] ?: continue
            conditionRules += Rule(
                id = condition.id,
                capture = reader.capture,
                names = condition.names,
                patterns = reader.patterns
            )
        }
        return RuleSet(attributeRules, conditionRules)
    }

}
