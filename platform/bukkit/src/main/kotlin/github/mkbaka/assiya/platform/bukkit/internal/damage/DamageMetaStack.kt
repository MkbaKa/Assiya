package github.mkbaka.assiya.platform.bukkit.internal.damage

import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import java.util.*

object DamageMetaStack {

    private val stacks = hashMapOf<UUID, ArrayDeque<DamageMeta>>()

    @JvmStatic
    fun getCurrent(uuid: UUID): DamageMeta? = stacks[uuid]?.peekLast()

    fun push(uuid: UUID, meta: DamageMeta) {
        stacks.computeIfAbsent(uuid) { ArrayDeque() }.addLast(meta)
    }

    fun pop(uuid: UUID) {
        val stack = stacks[uuid]!!
        stack.removeLast()
        if (stack.isEmpty()) {
            stacks.remove(uuid)
        }
    }

}
