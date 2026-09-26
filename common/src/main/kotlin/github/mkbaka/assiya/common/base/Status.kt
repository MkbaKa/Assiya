package github.mkbaka.assiya.common.base

/**
 * 匹配/提取后的结果
 *
 * capture id 指向读取后的值
 * 读取后就应该是不可变对象了 可以异步随便读
 */
data class Status<T : Any> private constructor(
    private val values: Map<String, T>
) {

    constructor(vararg values: Pair<String, T>) : this(values.toMap())

    val isEmpty: Boolean get() = values.isEmpty()

    val entries: Set<Map.Entry<String, T>> get() = values.entries

    operator fun get(captureId: String): T? = values[captureId]

    companion object {

        fun <T : Any> empty(): Status<T> = Status(emptyMap())

        fun <T : Any> of(map: Map<String, T>): Status<T> = Status(map.toMap())

    }

}