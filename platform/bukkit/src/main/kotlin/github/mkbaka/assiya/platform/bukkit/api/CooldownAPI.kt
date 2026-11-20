package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object CooldownAPI {

    @JvmStatic
    fun getRemainingMillis(session: AssiyaSession, id: String): Long =
        session.cooldown.getRemaining(id).inWholeMilliseconds

    @JvmStatic
    fun getRemaining(session: AssiyaSession, id: String): Duration = session.cooldown.getRemaining(id)

    @JvmStatic
    fun set(session: AssiyaSession, id: String, durationMillis: Long) =
        session.cooldown.set(id, durationMillis.milliseconds)

    @JvmStatic
    fun set(session: AssiyaSession, id: String, duration: Duration) = session.cooldown.set(id, duration)

    @JvmStatic
    fun edit(session: AssiyaSession, id: String, transform: (Duration) -> Duration): Duration =
        session.cooldown.edit(id, transform)

    @JvmStatic
    fun add(session: AssiyaSession, id: String, durationMillis: Long): Long =
        session.cooldown.add(id, durationMillis.milliseconds).inWholeMilliseconds

    @JvmStatic
    fun add(session: AssiyaSession, id: String, duration: Duration): Duration =
        session.cooldown.add(id, duration)

    @JvmStatic
    fun sub(session: AssiyaSession, id: String, durationMillis: Long): Long =
        session.cooldown.sub(id, durationMillis.milliseconds).inWholeMilliseconds

    @JvmStatic
    fun sub(session: AssiyaSession, id: String, duration: Duration): Duration =
        session.cooldown.sub(id, duration)

    @JvmStatic
    fun isExpired(session: AssiyaSession, id: String): Boolean = session.cooldown.isExpired(id)

    @JvmStatic
    fun tryAcquire(session: AssiyaSession, id: String, durationMillis: Long): Boolean =
        session.cooldown.tryAcquire(id, durationMillis.milliseconds)

    @JvmStatic
    fun tryAcquire(session: AssiyaSession, id: String, duration: Duration): Boolean =
        session.cooldown.tryAcquire(id, duration)

    @JvmStatic
    fun remove(session: AssiyaSession, id: String) = session.cooldown.remove(id)

    @JvmStatic
    fun clear(session: AssiyaSession) = session.cooldown.clear()

}
