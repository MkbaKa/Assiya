package github.mkbaka.assiya.platform.bukkit.internal.script.tools

import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.callSync
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.function.Consumer

class ScriptListener<T : Event>(
    private val eventClass: Class<T>
) {

    private var eventPriority: EventPriority = EventPriority.NORMAL

    private var ignoreCancelled: Boolean = false

    private lateinit var listener: Listener

    private lateinit var handler: Consumer<T>

    private lateinit var unregister: EventUtils.Unregister

    fun setEventPriority(eventPriority: EventPriority): ScriptListener<T> {
        this.eventPriority = eventPriority
        return this
    }

    fun setIgnoreCancelled(ignoreCancelled: Boolean): ScriptListener<T> {
        this.ignoreCancelled = ignoreCancelled
        return this
    }

    fun setListener(listener: Listener): ScriptListener<T> {
        this.listener = listener
        return this
    }

    fun setHandler(handler: Consumer<T>): ScriptListener<T> {
        this.handler = handler
        return this
    }

    fun register() {
        callSync {
            if (!this::listener.isInitialized) {
                listener = object : Listener {}
            }

            unregister = EventUtils.listen(eventClass, eventPriority, ignoreCancelled, listener, handler)
            listeners.add(this)
        }
    }

    fun unregister() {
        callSync {
            unregister.unregister()
            listeners.remove(this)
        }
    }

    companion object {

        private val listeners = hashSetOf<ScriptListener<*>>()

        fun unregister() {
            callSync {
                for (listener in listeners.toList()) {
                    listener.unregister()
                }
                listeners.clear()
            }
        }

        @JvmStatic
        fun <T : Event> create(event: Class<T>) = ScriptListener(event)

    }

}
