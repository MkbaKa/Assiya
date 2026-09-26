package github.mkbaka.assiya.platform.bukkit.internal.formula

object FormulaCompiler {

    private enum class Paren { LEFT, RIGHT }

    private data object Comma

    fun compile(id: String, expression: String, prefix: Char = '<', suffix: Char = '>'): Formula {
        require(prefix != suffix) {
            "Formula $id placeholder prefix and suffix must be different characters"
        }

        val postfix = toPostfix(id, tokenize(id, expression, prefix, suffix))
        validate(id, postfix)
        return Formula(id, postfix)
    }

    private fun tokenize(
        id: String,
        expression: String,
        prefix: Char,
        suffix: Char
    ): List<Any> {
        val tokens = arrayListOf<Any>()
        var index = 0

        while (index < expression.length) {
            val char = expression[index]
            when {
                // 空格跳过
                char.isWhitespace() -> index++

                // 变量左括号
                char == prefix -> {
                    // 取右括号位置
                    val end = expression.indexOf(suffix, index + 1)
                    // 没有右括号
                    if (end < 0) fail(id, "placeholder not has right bracket")
                    // 截出变量名
                    val name = expression.substring(index + 1, end).trim()
                    // 变量名是空的
                    if (name.isEmpty()) fail(id, "placeholder is empty")
                    tokens += FormulaToken.Variable(name)
                    // 指针移到变量结束点
                    index = end + 1
                }

                // 明文数字
                char.isDigit() || char == '.' -> {
                    val start = index
                    // 往右找到数字结尾
                    while (index < expression.length && (expression[index].isDigit() || expression[index] == '.')) {
                        index++
                    }
                    val raw = expression.substring(start, index)
                    val value = raw.toDoubleOrNull() ?: fail(id, "invalid number: $raw")
                    tokens += FormulaToken.Literal(value)
                }

                // 函数调用或者不带括号的变量名
                char.isLetter() || char == '_' -> {
                    val start = index
                    // 结束点
                    while (index < expression.length && (expression[index].isLetter() || expression[index] == '_')) {
                        index++
                    }
                    // 函数/变量名
                    val name = expression.substring(start, index)
                    // 如果名字后面跟着 ( 说明是函数调用
                    if (index < expression.length && expression[index] == '(') {
                        val function = FormulaFunction.of(name) ?: fail(id, "unknown function: $name")
                        tokens += FormulaToken.Fn(function)
                    } else {
                        // 反之为变量名
                        tokens += FormulaToken.Variable(name)
                    }
                }

                // 函数参数中间的逗号
                char == ',' -> {
                    tokens += Comma
                    index++
                }

                char == '(' -> {
                    tokens += Paren.LEFT
                    index++
                }

                char == ')' -> {
                    tokens += Paren.RIGHT
                    index++
                }

                // 运算符
                else -> {
                    val previous = tokens.lastOrNull()
                    // 一元运算的正负号
                    val unaryPosition = previous == null || previous is FormulaToken.Op
                        || previous == Paren.LEFT || previous == Comma
                    if (unaryPosition && (char == '+' || char == '-')) {
                        // 只有负号要单独处理
                        if (char == '-') {
                            tokens += FormulaToken.Op(FormulaOperator.NEGATE)
                        }
                    } else {
                        // 二元运算
                        val operator = FormulaOperator.binaryOf(char) ?: fail(id, "binary operator not supported")
                        tokens += FormulaToken.Op(operator)
                    }
                    index++
                }
            }
        }

        return tokens
    }

    private fun toPostfix(id: String, tokens: List<Any>): List<FormulaToken> {
        val output = arrayListOf<FormulaToken>()
        // 运算符或者括号
        val operators = ArrayDeque<Any>()

        for (token in tokens) {
            when (token) {
                is FormulaToken.Op -> {
                    val operator = token.operator
                    while (true) {
                        val top = operators.lastOrNull() as? FormulaOperator ?: break
                        val pops = top.priority > operator.priority
                            || (top.priority == operator.priority && !operator.rightAssociative)
                        if (!pops) break
                        operators.removeLast()
                        output += FormulaToken.Op(top)
                    }
                    operators.addLast(operator)
                }

                is FormulaToken.Fn -> operators.addLast(token)

                is FormulaToken -> output += token

                Paren.LEFT -> operators.addLast(token)

                Comma -> {
                    while (true) {
                        val top = operators.lastOrNull() ?: fail(id, "Unexpected comma position")
                        if (top == Paren.LEFT) break
                        operators.removeLast()
                        output += FormulaToken.Op(top as FormulaOperator)
                    }
                }

                Paren.RIGHT -> {
                    while (true) {
                        val top = operators.removeLastOrNull() ?: fail(id, "Unmatched parenthesis")
                        if (top == Paren.LEFT) break
                        output += FormulaToken.Op(top as FormulaOperator)
                    }
                    val top = operators.lastOrNull()
                    if (top is FormulaToken.Fn) {
                        operators.removeLast()
                        output += top
                    }
                }
            }
        }

        while (operators.isNotEmpty()) {
            val top = operators.removeLast()
            if (top !is FormulaOperator) fail(id, "Unmatched parenthesis")
            output += FormulaToken.Op(top)
        }

        return output
    }

    private fun validate(id: String, postfix: List<FormulaToken>) {
        var operands = 0
        for (token in postfix) {
            when (token) {
                is FormulaToken.Op -> {
                    val required = if (token.operator.unary) 1 else 2
                    if (operands < required) fail(id, "Invalid expression format")
                    operands -= required - 1
                }

                is FormulaToken.Fn -> {
                    val params = token.function.params
                    if (operands < params) fail(id, "Function ${token.function.id} requires $params arguments")
                    operands = params - 1
                }

                else -> operands++
            }
        }
        if (operands != 1) fail(id, "Invalid expression format")
    }

    private fun fail(id: String, message: String): Nothing {
        throw IllegalArgumentException("failed to compile formula $id: $message")
    }

}
