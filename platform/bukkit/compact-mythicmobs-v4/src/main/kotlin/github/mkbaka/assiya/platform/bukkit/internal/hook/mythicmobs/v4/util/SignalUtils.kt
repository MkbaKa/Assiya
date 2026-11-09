package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.util

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.skills.AbstractSkill
import io.lumine.xikage.mythicmobs.skills.IParentSkill
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.SkillTrigger
import io.lumine.xikage.mythicmobs.skills.variables.Variable
import java.lang.invoke.MethodHandles
import kotlin.jvm.optionals.getOrNull

object SignalUtils {

    private val lastSignal = ActiveMob::class.java.getDeclaredField("lastSignal").let { field ->
        field.isAccessible = true
        MethodHandles.lookup().unreflectSetter(field)
    }

    fun publish(
        target: AbstractEntity,
        attacker: AbstractEntity,
        signal: String,
        variables: Map<String, Variable>
    ): Boolean {
        val activeMob = AbstractSkill.getPlugin().mobManager.getActiveMob(target.uniqueId).getOrNull() ?: return false

        val meta = buildSignalSkillMeta(activeMob, attacker)

        meta.variables.putAll(variables)

        signal(activeMob, signal, meta)

        return meta.callingEvent.cancelled
    }

    fun signal(mob: ActiveMob, signal: String, meta: SkillMetadata) {
        lastSignal.invokeExact(mob, signal)
        mob.type.executeSignalSkill(signal, meta)
    }

    private fun buildSignalSkillMeta(
        activeMob: ActiveMob,
        trigger: AbstractEntity
    ): SkillMetadata {
        val meta = SkillMetadata(SkillTrigger.SIGNAL, activeMob, trigger)

        meta.entityTargets = hashSetOf(trigger)

        val caller = CancellableCaller()

        meta.power = activeMob.power
        meta.callingEvent = caller

        return meta
    }

    private class CancellableCaller : IParentSkill {

        private var cancelled = false

        override fun setCancelled() {
            cancelled = true
        }

        override fun getCancelled(): Boolean {
            return cancelled
        }

    }

}
