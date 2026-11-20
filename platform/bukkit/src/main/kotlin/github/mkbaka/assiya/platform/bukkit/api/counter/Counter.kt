package github.mkbaka.assiya.platform.bukkit.api.counter

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class Counter internal constructor(val type: CounterResetType) {
    private var value: Double? = null
    private var content: String? = null
    private val children = linkedMapOf<String, Child>()
    private var resetAt: TimeMark? = null
    private var resetPeriod: Duration? = null

    private fun checkReset() {
        val iterator = children.values.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().expireAt?.hasPassedNow() == true) iterator.remove()
        }
        if (resetAt?.hasPassedNow() == true) {
            value = null
            content = null
            resetAt = resetPeriod?.let { TimeSource.Monotonic.markNow() + it }
        }
    }

    @Synchronized
    @JvmOverloads
    fun getValue(default: Double = 0.0, child: Boolean = false): Double {
        checkReset()
        val sum = if (child) children.values.sumOf { it.value } else 0.0
        return if (value == null && sum == 0.0) default else (value ?: 0.0) + sum
    }

    @Synchronized
    fun setValue(next: Double): Double {
        checkReset()
        value = next
        return next
    }

    /** 回调在锁内执行, 不应修改其他 Counter. */
    @Synchronized
    fun editValue(default: Double, transform: (Double) -> Double): Double {
        checkReset()
        val next = transform(value ?: default)
        value = next
        return next
    }

    @JvmOverloads
    fun add(amount: Double, default: Double = 0.0): Double = editValue(default) { it + amount }

    @JvmOverloads
    fun sub(amount: Double, default: Double = 0.0): Double = editValue(default) { it - amount }

    @Synchronized
    fun resetValue() {
        checkReset()
        value = null
    }

    @Synchronized
    fun isDefaultValue(): Boolean {
        checkReset()
        return value == null
    }

    @Synchronized
    fun getContent(default: String): String {
        checkReset()
        return content ?: default
    }

    @Synchronized
    fun setContent(next: String): String {
        checkReset()
        content = next
        return next
    }

    @Synchronized
    fun resetContent() {
        checkReset()
        content = null
    }

    @Synchronized
    fun isDefaultContent(): Boolean {
        checkReset()
        return content == null
    }

    @Synchronized
    @JvmOverloads
    fun addChildValue(name: String, value: Double, duration: Duration? = null) {
        checkReset()
        children[name] = Child(value, duration?.let { TimeSource.Monotonic.markNow() + it })
    }

    @Synchronized
    fun getChildOrNull(name: String): Double? {
        checkReset()
        return children[name]?.value
    }

    @Synchronized
    fun getChildValue(): Double {
        checkReset()
        return children.values.sumOf { it.value }
    }

    @Synchronized
    fun isChildValueEmpty(): Boolean {
        checkReset()
        return children.isEmpty()
    }

    @Synchronized
    fun removeChild(name: String) {
        checkReset()
        children.remove(name)
    }

    @Synchronized
    fun resetChildValue() {
        checkReset()
        children.clear()
    }

    /** 按添加顺序扣除非负子值, 返回未扣除的余量. */
    @Synchronized
    fun takeChildValue(amount: Double): Double {
        require(amount.isFinite() && amount >= 0.0) { "amount must be finite and non-negative" }
        checkReset()
        var remaining = amount
        val iterator = children.values.iterator()
        while (remaining > 0.0 && iterator.hasNext()) {
            val child = iterator.next()
            if (child.value <= 0.0) continue
            val taken = minOf(remaining, child.value)
            child.value -= taken
            remaining -= taken
            if (child.value == 0.0) iterator.remove()
        }
        return remaining
    }

    @Synchronized
    @JvmOverloads
    fun setNextAutoReset(duration: Duration, repeat: Boolean = true) {
        require(duration.isFinite() && (!repeat || duration > Duration.ZERO)) {
            "duration must be finite and positive when repeating"
        }
        checkReset()
        resetPeriod = if (repeat) duration else null
        resetAt = TimeSource.Monotonic.markNow() + duration
    }

    @Synchronized
    fun cancelAutoReset() {
        checkReset()
        resetAt = null
        resetPeriod = null
    }

    @Synchronized
    fun reset() {
        checkReset()
        value = null
        content = null
        children.clear()
    }

    private class Child(var value: Double, val expireAt: TimeMark?)
}
