package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.mechanic

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.internal.util.entityOf
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import org.bukkit.entity.LivingEntity

@MythicMechanic("assiya:damage-meta", "assiya:damagemeta", "assiya:dm")
class DamageMetaMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : AssiyaDamageMechanic(container, mlc, IGNORE), ITargetedEntitySkill {

    /**
     * - assiya:damage-meta{attack|min=100;attack|max=200} @EIR{r=10}
     */

    private val attacker = mlc.getPlaceholderString(arrayOf("attacker", "player", "from", "source"), "")
    private val isClear = mlc.getPlaceholderString(arrayOf("clear", "c", "isClear"), "false")
    private val overwrite = mlc.getPlaceholderString(arrayOf("overwrite", "ow"), "false")

    private val usableAttribute = mlc.getString(arrayOf("usableAttribute", "ua"), null)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }

    private val disableAttribute = mlc.getString(arrayOf("disableAttribute", "da"), null)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()


    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val target = ae.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET

        val selected = Utils.selectDamageSubscribers(usableAttribute)

        // 指定了可用属性但没有对应 handler 时不执行后续流程
        if (selected != null && selected.isEmpty()) return SkillResult.INVALID_CONFIG

        val caster = meta.caster.entity.bukkitEntity

        // 找不到对应攻击者则默认施法者
        val attacker = entityOf(attacker.get(meta, ae)) ?: caster

        // 解析参数和属性
        val parsed = Utils.parseAllEntries(variables, meta, ae)
        val overwrite = overwrite.get(meta, ae).toBoolean()
        val attackerSnapshot = Utils.buildDamageAttackerSnapshot(attacker, parsed, isClear.get(meta, ae).toBoolean(), overwrite)

        // 上下文
        val processor = DamageProcessor.ofSnapshot(
            attacker = attacker,
            defender = target,
            subscribers = selected,
            attackerSnapshot = attackerSnapshot
        ) { pcb ->
            Utils.disableAttributes(pcb, Utils.resolveAttributeIds(disableAttribute))
            pcb.applyContext(meta, parsed)
        }

        val realCaster = when {
            attacker.uniqueId == caster.uniqueId -> meta.caster

            else -> {
                MythicBukkit.inst().skillManager.getCaster(BukkitAdapter.adapt(attacker))
            }
        }

        return dispatch(meta, ae, processor, realCaster)
    }


    companion object {

        private val IGNORE = arrayOf(
            "attacker", "player", "from", "source", "source",
            "preventKnockback", "pkb", "pk",
            "ignoreImmunity", "im", "pi", "ii",
            "noDamageTicks", "ndt",
            "clear", "c", "isClear",
            "signal", "s",
            "overwrite", "ow",
            "usableAttribute", "ua",
            "disableAttribute", "da",
            "postAttackSkill", "postAttack", "pas"
        ).mapTo(hashSetOf()) { it.lowercase() }

    }

}
