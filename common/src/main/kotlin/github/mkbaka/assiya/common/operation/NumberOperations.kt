package github.mkbaka.assiya.common.operation

/**
 * 数字类的运算操作
 */
interface NumberOperations : Operation<Double> {

    data object Add : NumberOperations {
        override fun merge(current: Double?, incoming: Double): Double {
            return (current ?: 0.0) + incoming
        }
    }

    data object Multiply : NumberOperations {
        override fun merge(current: Double?, incoming: Double): Double {
            return (current ?: 1.0) * incoming
        }
    }

    data object Max : NumberOperations {
        override fun merge(current: Double?, incoming: Double): Double {
            return current?.coerceAtLeast(incoming) ?: incoming
        }
    }

    data object Min : NumberOperations {
        override fun merge(current: Double?, incoming: Double): Double {
            return current?.coerceAtMost(incoming) ?: incoming
        }
    }

}