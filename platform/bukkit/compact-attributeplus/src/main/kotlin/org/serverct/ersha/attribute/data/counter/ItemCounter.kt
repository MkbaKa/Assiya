package org.serverct.ersha.attribute.data.counter

import github.mkbaka.assiya.platform.bukkit.api.counter.Counter
import kotlin.time.Duration.Companion.seconds

class ItemCounter internal constructor(private val counter: Counter) {
    fun getValue(default: Double): Double = counter.getValue(default)
    fun getValue(default: Double, child: Boolean): Double = counter.getValue(default, child)
    fun setValue(next: Double): Double = counter.setValue(next)
    fun updateValue(update: Double, default: Double): Double = counter.add(update, default)
    fun resetValue() = counter.resetValue()
    fun isDefaultValue(): Boolean = counter.isDefaultValue()
    fun updateContent(update: String): String = counter.setContent(update)
    fun getContent(default: String): String = counter.getContent(default)
    fun resetContent() = counter.resetContent()
    fun isDefaultContent(): Boolean = counter.isDefaultContent()
    fun resetChildValue() = counter.resetChildValue()
    fun takeChildValue(value: Double): Double = counter.takeChildValue(value)

    @JvmOverloads
    fun addChildValue(name: String, value: Double, time: Double = -1.0) =
        counter.addChildValue(name, value, if (time > 0.0) time.seconds else null)

    fun getChildValue(): Double = counter.getChildValue()
    fun isChildValueEmpty(): Boolean = counter.isChildValueEmpty()
    fun removeChild(name: String) = counter.removeChild(name)

    @JvmOverloads
    fun setNextAutoReset(seconds: Double, autoNext: Boolean = true) =
        counter.setNextAutoReset(seconds.seconds, autoNext)
}
