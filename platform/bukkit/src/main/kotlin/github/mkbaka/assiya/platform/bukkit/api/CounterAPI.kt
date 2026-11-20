package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.platform.bukkit.api.counter.Counter
import github.mkbaka.assiya.platform.bukkit.api.counter.CounterResetType
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession

object CounterAPI {
    @JvmStatic
    @JvmOverloads
    fun getOrCreate(session: AssiyaSession, key: String, type: CounterResetType = CounterResetType.DEFAULT): Counter =
        session.counter.getOrCreate(key, type)

    @JvmStatic
    fun get(session: AssiyaSession, key: String): Counter? = session.counter[key]

    @JvmStatic
    fun contains(session: AssiyaSession, key: String): Boolean = session.counter.contains(key)

    @JvmStatic
    fun remove(session: AssiyaSession, key: String) = session.counter.remove(key)

    @JvmStatic
    fun clear(session: AssiyaSession) = session.counter.clear()
}
