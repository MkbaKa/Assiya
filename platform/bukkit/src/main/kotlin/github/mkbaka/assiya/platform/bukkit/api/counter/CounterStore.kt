package github.mkbaka.assiya.platform.bukkit.api.counter

import java.util.concurrent.ConcurrentHashMap

enum class CounterResetType { DEFAULT, DEATH }

class CounterStore {
    private val counters = ConcurrentHashMap<String, Counter>()

    @JvmOverloads
    fun getOrCreate(key: String, type: CounterResetType = CounterResetType.DEFAULT): Counter =
        counters.computeIfAbsent(key) { Counter(type) }

    operator fun get(key: String): Counter? = counters[key]

    fun contains(key: String): Boolean = counters.containsKey(key)

    fun remove(key: String) {
        counters.remove(key)
    }

    fun clear() {
        counters.clear()
    }

    fun resetDeath() {
        for (counter in counters.values) {
            if (counter.type == CounterResetType.DEATH) counter.reset()
        }
    }
}
