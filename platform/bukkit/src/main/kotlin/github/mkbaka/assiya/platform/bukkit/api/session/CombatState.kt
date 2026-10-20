package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
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
        targetRef = target?.let { WeakReference(it) }
        inCombat = true
        EntityTaskAPI.runTaskKt(owner, TASK_ID) {
            key = TASK_ID
            delay = (Config.global.COMBAT_DURATION * 20).toLong()
            params = hashMapOf(
                "state" to this
            )
        }
    }

    fun leave() {
        clear()
        EntityTaskAPI.cancelTask(session.ownerUUID, TASK_ID)
    }

    private fun clear() {
        targetRef = null
        inCombat = false
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
