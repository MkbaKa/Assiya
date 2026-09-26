package github.mkbaka.assiya.platform.bukkit.internal.formula

import java.util.concurrent.ThreadLocalRandom

/**
 * 内置函数
 */
enum class FormulaFunction(
    val id: String,
    val params: Int,
    val apply: (List<Double>) -> Double
) {

    RANDOM("random", 2, { (from, to) -> from + (to - from) * ThreadLocalRandom.current().nextDouble() }),

    MIN("min", 2, { (a, b) -> minOf(a, b) }),

    MAX("max", 2, { (a, b) -> maxOf(a, b) }),

    ;

    companion object {

        private val byId = entries.associateBy { it.id }

        fun of(id: String): FormulaFunction? = byId[id]

    }

}
