package github.mkbaka.assiya.platform.bukkit.internal.damage

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.event.EventPriority
import java.util.*

object DamageMetaStack {

    private val stacks = hashMapOf<UUID, ArrayDeque<DamageMeta>>()

    @JvmStatic
    fun getCurrent(uuid: UUID): DamageMeta? = stacks[uuid]?.peekLast()

    fun push(uuid: UUID, meta: DamageMeta) {
        stacks.getOrPut(uuid) { ArrayDeque() }.addLast(meta)
    }

    fun pop(uuid: UUID) {
        val stack = stacks[uuid]!!
        stack.removeLast()
        if (stack.isEmpty()) {
            stacks.remove(uuid)
        }
    }

    // TODO entity data move to session ?
    @Listener(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun remove(event: EntityRemoveFromWorldEvent) {
        stacks.remove(event.entity.uniqueId)
    }

}
