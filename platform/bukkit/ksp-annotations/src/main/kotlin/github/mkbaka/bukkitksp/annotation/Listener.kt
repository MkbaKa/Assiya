package github.mkbaka.bukkitksp.annotation

import org.bukkit.event.EventPriority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Listener(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false
)
