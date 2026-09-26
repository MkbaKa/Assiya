package github.mkbaka.assiya.common.parser

import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import java.util.function.Predicate

class AttributeParsedSource(
    val parseds: List<Parsed>
) {

    fun evaluateConditions(
        evaluator: Predicate<ParsedCondition>
    ): BooleanArray {
        val results = BooleanArray(parseds.size)

        for ((index, parsed) in parseds.withIndex()) {
            when (parsed) {
                is Parsed.Attribute -> Unit
                is Parsed.SkipCondition -> {
                    results[index] = parsed.conditions.all(evaluator::test)
                }
                is Parsed.SourceCondition -> {
                    results[index] = parsed.conditions.all(evaluator::test)
                    if (!results[index]) break
                }
            }
        }

        return results
    }

    fun resolve(
        conditions: BooleanArray
    ): List<AttributeValue<*>> {
        val accepted = arrayListOf<AttributeValue<*>>()
        var skip = 0

        /**
         * 如果把整个 part 的解析结果看作 Stack
         * 那么把结果都压进栈里就是
         *  属性即栈中的元素
         *  单行/来源级的条件为对n个元素影响的运算符
         *  单行条件是有限操作 即跳过 n 次弹栈
         *  来源条件不满足这个栈中的所有元素都没有存在的必要了
         */
        for ((index, parsed) in parseds.withIndex()) {
            when (parsed) {
                is Parsed.Attribute -> {
                    if (skip > 0) {
                        skip--
                    } else {
                        accepted += parsed.value
                    }
                }

                is Parsed.SkipCondition -> {
                    if (!conditions[index]) {
                        skip += parsed.attributeCount
                    }
                }

                is Parsed.SourceCondition -> {
                    if (!conditions[index]) {
                        return emptyList()
                    }
                }
            }
        }

        return merge(accepted)
    }

    private fun merge(values: List<AttributeValue<*>>): List<AttributeValue<*>> {
        return values.groupBy { it.attributeId }.map { (_, group) -> mergeGroup<Any>(group) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> mergeGroup(values: List<AttributeValue<*>>): AttributeValue<T> {
        val first = values.first() as AttributeValue<T>
        return AttributeValue(
            attributeId = first.attributeId,
            status = first.capture.merge(values.map { (it as AttributeValue<T>).status }),
            capture = first.capture,
        )
    }

    sealed interface Parsed {

        class Attribute(val value: AttributeValue<*>) : Parsed

        class SkipCondition(
            val conditions: List<ParsedCondition>,
            val attributeCount: Int
        ) : Parsed

        class SourceCondition(val conditions: List<ParsedCondition>) : Parsed

    }

    companion object {

        val EMPTY = AttributeParsedSource(emptyList())

    }

}