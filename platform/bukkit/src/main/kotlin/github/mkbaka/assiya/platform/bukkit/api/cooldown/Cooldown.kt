package github.mkbaka.assiya.platform.bukkit.api.cooldown

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class Cooldown internal constructor(private val markNow: () -> TimeMark) {

    constructor() : this({ TimeSource.Monotonic.markNow() })

    private val coolings = ConcurrentHashMap<String, Cooling>()

    fun getRemaining(key: String): Duration {
        var remaining = Duration.ZERO
        coolings.computeIfPresent(key) { _, entry ->
            remaining = entry.remaining()
            if (remaining == Duration.ZERO) null else entry
        }
        return remaining
    }

    fun set(key: String, duration: Duration) {
        coolings.compute(key) { _, _ ->
            if (duration == Duration.ZERO) null else Cooling(markNow(), duration)
        }
    }

    fun edit(key: String, transform: (Duration) -> Duration): Duration {
        var updated = Duration.ZERO
        coolings.compute(key) { _, cooling ->
            val startedAt = markNow()
            val remaining = cooling?.remaining() ?: Duration.ZERO
            val result = transform(remaining)

            updated = result.coerceAtLeast(Duration.ZERO)
            if (updated == Duration.ZERO) null else Cooling(startedAt, updated)
        }
        return updated
    }

    fun add(key: String, duration: Duration): Duration = edit(key) { it + duration }

    fun sub(key: String, duration: Duration): Duration = edit(key) { it - duration }

    fun isExpired(key: String): Boolean {
        var expired = true
        coolings.computeIfPresent(key) { _, entry ->
            expired = entry.isExpired()
            if (expired) null else entry
        }
        return expired
    }

    fun tryAcquire(key: String, duration: Duration): Boolean {
        var acquired = false
        coolings.compute(key) { _, entry ->
            if (entry != null && !entry.isExpired()) {
                entry
            } else {
                acquired = true
                if (duration == Duration.ZERO) null else Cooling(markNow(), duration)
            }
        }
        return acquired
    }

    fun remove(key: String) {
        coolings.remove(key)
    }

    fun clear() {
        coolings.clear()
    }

    private data class Cooling(val startedAt: TimeMark, val duration: Duration) {

        val expireAt: TimeMark = startedAt + duration

        fun isExpired(): Boolean = expireAt.hasPassedNow()

        fun remaining(): Duration = (-expireAt.elapsedNow()).coerceAtLeast(Duration.ZERO)

    }

}
