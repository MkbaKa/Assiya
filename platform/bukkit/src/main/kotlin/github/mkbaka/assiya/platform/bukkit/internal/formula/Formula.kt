package github.mkbaka.assiya.platform.bukkit.internal.formula

import github.mkbaka.assiya.platform.bukkit.internal.util.TypeUtils.ofDouble
import github.mkbaka.assiya.platform.bukkit.internal.util.severe

class Formula(
    val id: String,
    private val postfix: List<FormulaToken>
) {

    fun invoke(resolve: (String) -> Any?): Double {
        val stack = ArrayDeque<Double>()
        for (token in postfix) {
            when (token) {
                // 明文数字直接入栈
                is FormulaToken.Literal -> stack.addLast(token.value)

                // 解析变量后再入栈
                is FormulaToken.Variable -> {
                    val value = resolve(token.name)
                        ?: severe("Formula $id has invalid variable ${token.name}")
                    stack.addLast(value.ofDouble())
                }

                // 运算符
                is FormulaToken.Op -> {
                    val operator = token.operator
                    val right = stack.removeLast()
                    // 一元运算不需要左边的值 随便捏一个给他
                    // 二元则从栈里取另外一个值
                    val left = if (operator.unary) 0.0 else stack.removeLast()
                    stack.addLast(operator.apply(left, right))
                }

                // 函数调用
                is FormulaToken.Fn -> {
                    // 函数需要的参数长度
                    val params = token.function.params
                    // 参数列表
                    val args = MutableList(params) { 0.0 }
                    for (i in params - 1 downTo 0) {
                        args[i] = stack.removeLast()
                    }
                    // 函数结果入栈
                    stack.addLast(token.function.apply(args))
                }
            }
        }

        // 一顿叽里咕噜操作之后 剩下的那个就是计算结果
        return stack.last()
    }

}

sealed interface FormulaToken {

    data class Literal(val value: Double) : FormulaToken

    data class Variable(val name: String) : FormulaToken

    data class Op(val operator: FormulaOperator) : FormulaToken

    data class Fn(val function: FormulaFunction) : FormulaToken

}