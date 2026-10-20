package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI
import github.mkbaka.assiya.platform.bukkit.api.event.CombatJoinEvent
import github.mkbaka.assiya.platform.bukkit.api.event.CombatLeaveEvent
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call
import org.bukkit.entity.Entity
import java.lang.ref.WeakReference

class CombatState(private val session: AssiyaSession) {

    private var targetRef: WeakReference<Entity>? = null

    var inCombat: Boolean = false
        private set

    val target: Entity?
        get() = targetRef?.get()

    @JvmOverloads
    fun join(target: Entity? = null) {
        val owner = session.owner ?: return

        // 如果没换目标就不用重复触发事件
        val changed = !inCombat || target?.uniqueId != this.target?.uniqueId
        if (changed) {
            if (CombatJoinEvent.Pre(session, target).call()) return

            targetRef = target?.let { WeakReference(it) }
            inCombat = true
        }

        // 重复触发可以延后战斗结束的时间
        EntityTaskAPI.runTaskKt(owner, TASK_ID) {
            key = TASK_ID
            delay = (Config.global.COMBAT_DURATION * 20).toLong()
            params = hashMapOf(
                "state" to this@CombatState
            )
        }

        if (changed) {
            CombatJoinEvent.Post(session, target).call()
        }
    }

    fun leave() {
        clear()
        EntityTaskAPI.cancelTask(session.ownerUUID, TASK_ID)
    }

    private fun clear() {
        if (!inCombat) return

        val previousTarget = target
        targetRef = null
        inCombat = false

        CombatLeaveEvent(session, previousTarget).call()
    }

    companion object {

        private const val TASK_ID = "assiya:combat-state"

        init {
            EntityTaskAPI.registerTask(
                TASK_ID,
                task = { _, params -> (params["state"] as CombatState).clear() },
                onTerminate = { _, params -> (params["state"] as CombatState).clear() }
            )
        }

    }

}
