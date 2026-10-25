package github.mkbaka.assiya.compact.mythicmobs.v5.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v5.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetDefaultComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import org.bukkit.entity.LivingEntity

@MythicMechanic("assiya:damage-snapshot-dispatch", "assiya:damagesnapshotdispatch", "assiya:dsd")
class DamageSnapshotDispatchMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : AssiyaDamageMechanic(container, mlc, IGNORE), ITargetedEntitySkill {

    private val id = mlc.getPlaceholderString(arrayOf("id", "i"), "")

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val target = ae.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET

        val id = id.get(meta, ae)
        if (id.isEmpty()) return SkillResult.INVALID_CONFIG

        val cached = AssiyaAPI.getSession(target)
            ?.getMetadata<DamageProcessor>(DamageSnapshotMechanic.metadataKey(id))
            ?: return SkillResult.INVALID_CONFIG

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

