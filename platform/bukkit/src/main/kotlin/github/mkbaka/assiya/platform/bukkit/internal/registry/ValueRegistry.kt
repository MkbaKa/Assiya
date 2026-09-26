package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.base.Value


object ValueRegistry : Registry<Value<*>>("format") {

    fun <T : Any> of(key: String): Value<T> {
        return this[key] as Value<T>
    }

    init {
        register("decimal", Value.Number)
        register("text", Value.Text)

        // TODO move to config
        val romans = hashMapOf(
            'I' to 1,
            'V' to 5,
            'X' to 10,
            'L' to 50,
            'C' to 100,
            'D' to 500,
            'M' to 1000
        )
        register(
            "roman", Value.RomanNumber(
                romans.keys.toList(),
                romans::get
            )
        )
    }

}
