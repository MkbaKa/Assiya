package github.mkbaka.assiya.common.parser.part

import github.mkbaka.assiya.common.parser.RuleSet

interface AttributeSourcePartParserFactory {

    fun create(rules: RuleSet): AttributeSourcePartParser

}
