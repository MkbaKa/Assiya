package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.misc.Unregister
import org.bukkit.Bukkit
import org.bukkit.event.*
import java.util.function.Consumer

object EventUtils {

    /**
     * @return [Boolean] 是否被取消
     */
    fun Event.call(): Boolean {
        Bukkit.getPluginManager().callEvent(this)
        return this is Cancellable && (this as Cancellable).isCancelled
    }

    @JvmStatic
    fun <T : Event> listen(
        eventClass: Class<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: Listener = object : Listener {},
        executor: Consumer<T>
    ): Unregister {
        return on(eventClass, priority, ignoreCancelled, listener, executor::accept)
    }

    @JvmStatic
    inline fun <reified T : Event> on(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: Listener = object : Listener {},
        noinline executor: T.() -> Unit
    ) = on(T::class.java, priority, ignoreCancelled, listener, executor)

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : Event> on(
        eventClass: Class<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        listener: Listener = object : Listener {},
        executor: T.() -> Unit
    ): Unregister {
        Bukkit.getPluginManager()
            .registerEvent(
                eventClass, listener, priority, { _, event ->
                    if (eventClass.isAssignableFrom(event::class.java)) {
                        executor.invoke(event as T)
                    }
                }, AssiyaBukkitPlugin.INSTANCE, ignoreCancelled
            )

        return { unregister(listener) }
    }

    @JvmStatic
    fun unregister(listener: Listener) {
        HandlerList.unregisterAll(listener)
    }

}