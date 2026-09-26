package github.mkbaka.assiya.common.operation

/**
 * 文本类
 */
interface TextOperations : Operation<String> {

    data object Override : TextOperations {
        override fun merge(current: String?, incoming: String): String {
            return incoming
        }
    }

    data object Append : TextOperations {
        override fun merge(current: String?, incoming: String): String {
            return current.orEmpty() + incoming
        }
    }

    data object Skip : TextOperations {
        override fun merge(current: String?, incoming: String): String {
            return current ?: incoming
        }
    }

}