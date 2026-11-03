package github.mkbaka.assiya.common.condition

/**
 * 待判断条件的解析上下文
 */
open class ConditionContext(
    val captures: Map<String, Any>
) {

    @Suppress("UNCHECKED_CAST")
    fun <T> capture(name: String): T? = captures[name] as T?

    fun <T> capture(name: String, def: T): T = capture(name) ?: def

}
