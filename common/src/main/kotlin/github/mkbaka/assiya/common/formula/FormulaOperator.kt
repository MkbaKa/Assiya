package github.mkbaka.assiya.common.formula

import kotlin.math.pow

/**
 * 运算符
 */
enum class FormulaOperator(
    val symbol: Char,
    val priority: Int,
    val rightAssociative: Boolean = false,
    val unary: Boolean = false,
    val apply: (Double, Double) -> Double
) {

    ADD('+', 1, apply = { l, r -> l + r }),
    SUBTRACT('-', 1, apply = { l, r -> l - r }),
    MULTIPLY('*', 2, apply = { l, r -> l * r }),
    DIVIDE('/', 2, apply = { l, r -> l / r }),
    REMAINDER('%', 2, apply = { l, r -> l % r }),
    POWER('^', 3, rightAssociative = true, apply = { l, r -> l.pow(r) }),
    NEGATE('-', 4, rightAssociative = true, unary = true, apply = { _, r -> -r }),

    ;

    companion object {

        private val binaryBySymbol = entries.filter { !it.unary }.associateBy { it.symbol }

        fun binaryOf(symbol: Char): FormulaOperator? = binaryBySymbol[symbol]

    }


}
