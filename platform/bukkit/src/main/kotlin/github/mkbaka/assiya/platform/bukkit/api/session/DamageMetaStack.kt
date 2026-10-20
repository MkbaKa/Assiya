package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import java.util.*

class DamageMetaStack {

    private val stacks = ArrayDeque<DamageMeta>()

    val current: DamageMeta?
        get() = stacks.peekLast()

    fun push(damage: DamageMeta) {
        stacks.addLast(damage)
    }

    fun pop() {
        stacks.removeLast()
    }

}
