package github.mkbaka.assiya.common.parser

/**
 * 属性贡献/条件的作用域
 */
interface AttributeScope {

    data object Source : AttributeScope

    data class Line(val index: Int) : AttributeScope

}