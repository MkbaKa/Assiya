package github.mkbaka.assiya.compact.mythicmobs.v4.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v4.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetDefaultComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import org.bukkit.entity.LivingEntity

@MythicMechanic("assiya:damage-snapshot-dispatch", "assiya:damagesnapshotdispatch", "assiya:dsd")
class DamageSnapshotDispatchMechanic(
    mlc: MythicLineConfig
) : AssiyaDamageMechanic(mlc, IGNORE), ITargetedEntitySkill {

    private val id = mlc.getPlaceholderString(arrayOf("id", "i"), "")

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        val target = ae.bukkitEntity as? LivingEntity ?: return false

        val id = id.get(meta, ae)
        if (id.isEmpty()) return false

        val cached = AssiyaAPI.getSession(target)
            ?.getMetadata<DamageProcessor>(DamageSnapshotMechanic.metadataKey(id))
            ?: return false

        val parsed = Utils.parseAllEntries(variables, meta, ae)

        val processor = cached.fork { pcb ->
            pcb[TargetComponent.Key] = TargetDefaultComponent(target, AssiyaAPI.getSnapshot(target))
            pcb.applyContext(meta, parsed)
        }

        return dispatch(meta, ae, processor, meta.caster)
    }

    companion object {

        private val IGNORE = arrayOf(
            "id", "i",
            "preventKnockback", "pkb", "pk",
            "ignoreImmunity", "im", "pi", "ii",
            "noDamageTicks", "ndt",
            "signal", "s",
            "disableAttribute", "da",
            "postAttackSkill", "postAttack", "pas"
        ).mapTo(hashSetOf()) { it.lowercase() }

    }

}
