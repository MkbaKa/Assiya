package github.mkbaka.assiya.common.base

/**
 * 属性值的读取方式
 * 通过 regex 匹配值的文本 然后塞给 parse 变成值类型
 */
interface Value<T : Any> {

    val regex: String

    val defaultValue: T

    fun parse(raw: String): T?

    data object Number : Value<Double> {

        override val defaultValue: Double = 0.0

        // 正负符号可选 正数部分必须 小数部分可选
        override val regex: String = "[-+]?\\d+(?:\\.\\d+)?"

        override fun parse(raw: String): Double? = raw.toDoubleOrNull()

    }

    data object Text : Value<String> {

        override val defaultValue: String = ""

        override val regex: String = ".+"

        override fun parse(raw: String): String = raw
    }

    data class RomanNumber(
        private val keys: List<Char>,
        private val transformer: (Char) -> Int?
    ) : Value<Double> {

        override val defaultValue: Double = 0.0

        override val regex: String = "[${keys.joinToString("")}]+"

        override fun parse(raw: String): Double? {
            return parseRoman(raw)?.toDouble()
        }

        private fun parseRoman(raw: String): Int? {
            var total = 0
            var previous = 0

            for (char in raw.reversed()) {
                val value = transformer(char) ?: return null

                // "IV" -> 'V' 'I' -> value 解析到 I 时 previous 为 V 对应的 5
                // 1 < 5 得出 5 - 1 = 4
                if (value < previous) {
                    total -= value
                } else {
                    total += value
                    previous = value
                }
            }

            return total
        }
    }

}
