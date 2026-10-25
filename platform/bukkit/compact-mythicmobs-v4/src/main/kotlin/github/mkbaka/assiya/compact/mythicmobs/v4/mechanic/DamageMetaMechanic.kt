package github.mkbaka.assiya.compact.mythicmobs.v4.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v4.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.internal.util.entityOf
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.mobs.GenericCaster
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import org.bukkit.entity.LivingEntity
import kotlin.jvm.optionals.getOrNull

@MythicMechanic("assiya:damage-meta", "assiya:damagemeta", "assiya:dm")
class DamageMetaMechanic(
    mlc: MythicLineConfig
) : AssiyaDamageMechanic(mlc, IGNORE), ITargetedEntitySkill {

    /**
     * - assiya:damage-meta{physical-attack|min=100;physical-attack|max=200} @EIR{r=10}
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

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        val target = ae.bukkitEntity as? LivingEntity ?: return false

        val selected = Utils.selectDamageSubscribers(usableAttribute)

        // 指定了可用属性但没有对应 handler 时不执行后续流程
        if (selected != null && selected.isEmpty()) return false

        val caster = meta.caster.entity.bukkitEntity

        // 找不到对应攻击者则默认施法者
        val attacker = entityOf(attacker.get(meta, ae)) ?: caster

        // 解析参数和属性
        val parsed = Utils.parseAllEntries(variables, meta, ae)
        val overwrite = overwrite.get(meta, ae).toBoolean()
        val attackerSnapshot = Utils.buildDamageAttackerSnapshot(
            attacker,
            parsed,
            isClear.get(meta, ae).toBoolean(),
            overwrite
        )

        // 上下文
        val disabledIds = Utils.resolveAttributeIds(disableAttribute)
        val processor = DamageProcessor.ofSnapshot(
            attacker = attacker,
            defender = target,
            subscribers = selected,
            attackerSnapshot = attackerSnapshot
        ) { pcb ->
            Utils.disableAttributes(pcb, disabledIds)
            pcb.applyContext(meta, parsed)
        }

        val realCaster = when {
            attacker.uniqueId == caster.uniqueId -> meta.caster

            else -> {
                getPlugin().mobManager.getActiveMob(attacker.uniqueId)
                    .getOrNull() ?: GenericCaster(BukkitAdapter.adapt(attacker))
            }
        }

        // 伤害逻辑
        return dispatch(meta, ae, processor, realCaster)
    }

    companion object {

        private val IGNORE = arrayOf(
            "attacker", "player", "from", "source",
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
