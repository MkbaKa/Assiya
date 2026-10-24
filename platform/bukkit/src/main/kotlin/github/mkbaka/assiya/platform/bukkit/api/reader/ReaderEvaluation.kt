package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.base.Status

/**
 * 固定 [Status] 的返回结果
 * 可以用在 [github.mkbaka.assiya.platform.bukkit.internal.command.sub.StatusCommand] 这种需要两个公式结果之间相关联的地方
 * 如果不固定结果 那每次 [github.mkbaka.assiya.platform.bukkit.internal.formula.FormulaFunction.RANDOM] 都会返回不同的结果
 * 再如果 公式A 中使用了 公式B 恰好 公式B 里包含了 RANDOM 函数
 * 在不固定结果的情况下 random 的结果与 total 取值内再次执行的 random 会不一致
 */
open class ReaderEvaluation(
    val reader: Reader,
    val status: Status<*>
) {

    /**
     * 已经算出的 placeholder 结果
     */
    private val results = hashMapOf<String, Double>()

    /**
     * 正在求值的 placeholder
     * 防止递归
     */
    private val resolving = hashSetOf<String>()

    /**
     * 读取本次求值内的值
     *
     * 没有公式定义的 reader 直接按单次读取处理
     */
    open fun read(name: String): Any? {
        return reader.read(status, name)
    }

    /**
     * 求值一个具名 placeholder
     */
    protected fun resolve(name: String, calculator: () -> Double): Double {
        results[name]?.let { return it }

        check(resolving.add(name)) {
            "Placeholder $name in reader ${reader.id} contains a circular reference"
        }

        try {
            val value = calculator()
            results[name] = value
            return value
        } finally {
            resolving.remove(name)
        }
    }

}
