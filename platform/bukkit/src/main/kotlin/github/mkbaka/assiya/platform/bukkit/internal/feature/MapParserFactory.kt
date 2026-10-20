package github.mkbaka.assiya.platform.bukkit.internal.feature

import github.mkbaka.assiya.common.parser.RuleSet
import github.mkbaka.assiya.common.parser.part.AttributeSourcePartParser
import github.mkbaka.assiya.common.parser.part.AttributeSourcePartParserFactory
import github.mkbaka.assiya.common.parser.part.MapAttributeSourcePartParser

object MapParserFactory : AttributeSourcePartParserFactory {

    override fun create(rules: RuleSet): AttributeSourcePartParser {
        return MapAttributeSourcePartParser(rules)
    }

}
