package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.base.Value
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.bukkitksp.annotation.CustomTask


object ValueRegistry : NamedRegistry<Value<*>>("format") {

    private var roman: Value.RomanNumber? = null

    fun <T : Any> of(key: String): Value<T> {
        return this[key] as Value<T>
    }

    init {
        register("decimal", Value.Number)
        register("text", Value.Text)
    }

    @CustomTask(taskId = "reload", priority = 6)
    fun reload() {
        val values = Config.global.ROMAN_VALUES
        val next = Value.RomanNumber(values.keys.toList(), values::get)
        roman?.let { unregister("roman", it) }
        register("roman", next)
        roman = next
    }

}
