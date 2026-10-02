package github.mkbaka.assiya.compact.mythicmobs.v5.util

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.api.skills.IParentSkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.core.skills.SkillTriggers
import io.lumine.mythic.core.skills.SkillMetadataImpl
import io.lumine.mythic.core.constants.MobKeys
import org.bukkit.persistence.PersistentDataType
import io.lumine.mythic.core.skills.variables.Variable
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
        val activeMob = MythicBukkit.inst().mobManager.getActiveMob(target.uniqueId).getOrNull() ?: return false

        val meta = buildSignalSkillMeta(activeMob, attacker)

        meta.variables.putAll(variables)

        signal(activeMob, signal, meta)

        return meta.callingEvent.cancelled
    }

    fun signal(mob: ActiveMob, signal: String, meta: SkillMetadata) {
        lastSignal.invokeExact(mob, signal)
        mob.entity.dataContainer.set(MobKeys.LAST_SIGNAL, PersistentDataType.STRING, signal)
        mob.type.executeSignalSkill(signal, meta)
    }

    private fun buildSignalSkillMeta(
        activeMob: ActiveMob,
        trigger: AbstractEntity
    ): SkillMetadata {
        val meta = SkillMetadataImpl(SkillTriggers.SIGNAL, activeMob, trigger)

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
