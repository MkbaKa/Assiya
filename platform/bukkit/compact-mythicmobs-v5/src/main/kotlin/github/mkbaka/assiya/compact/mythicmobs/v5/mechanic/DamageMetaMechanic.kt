package github.mkbaka.assiya.compact.mythicmobs.v5.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v5.handler.component.TriggerBySkillComponent
import github.mkbaka.assiya.compact.mythicmobs.v5.util.DamageMetadataFactory
import github.mkbaka.assiya.compact.mythicmobs.v5.util.SignalUtils
import github.mkbaka.assiya.compact.mythicmobs.v5.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.internal.util.entityOf
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.Skill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableType
import org.bukkit.entity.LivingEntity
import kotlin.jvm.optionals.getOrNull

@MythicMechanic("assiya:damage-meta", "assiya:damagemeta")
class DamageMetaMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : SkillMechanic(container.manager, container.file, mlc.line, mlc), ITargetedEntitySkill {

    /**
     * - assiya:damage-meta{attack|min=100;attack|max=200} @EIR{r=10}
     */

    private val attacker = mlc.getPlaceholderString(arrayOf("attacker", "player", "from", "source"), "")
    private val preventKnockback = mlc.getPlaceholderString(arrayOf("preventKnockback", "pkb", "pk"), "false")
    private val ignoreImmunity = mlc.getPlaceholderString(arrayOf("ignoreImmunity", "im", "pi", "ii"), "false")
    private val noDamageTicks = mlc.getPlaceholderString(arrayOf("noDamageTicks", "ndt"), "20")
    private val isClear = mlc.getPlaceholderString(arrayOf("clear", "c", "isClear"), "false")
    private val signal = mlc.getPlaceholderString(arrayOf("signal", "s"), "")
    private val overwrite = mlc.getPlaceholderString(arrayOf("overwrite", "ow"), "false")
    private val disableAttribute = mlc.getString(arrayOf("disableAttribute", "da"), null)

    private var postAttackSkill: Skill? = null

    private val variables = mlc.entrySet().asSequence()
        .filter { it.key !in IGNORE }
        .associate { it.key to PlaceholderString.of(it.value) }

    init {
        val skillManager = MythicBukkit.inst().skillManager
        val postAttack = mlc.getString(arrayOf("pas", "postAttack", "postAttackSkill"), "")

        if (postAttack.isNotEmpty()) {
            skillManager.queueAfterLoad {
                postAttackSkill = skillManager.getSkill(postAttack).getOrNull()
            }
        }

        isAsyncSafe = false
    }

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val target = ae.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val caster = meta.caster.entity.bukkitEntity

        // 找不到对应攻击者则默认施法者
        val attacker = entityOf(attacker.get(meta, ae)) ?: caster

        // 解析参数和属性
        val parsed = Utils.parseAllEntries(variables, meta, ae)
        val attributes = Utils.parseAttributes(attacker, parsed)

        // 上下文
        val processor = DamageProcessor.of(
            attacker = attacker,
            defender = target,
            attackerSnapshot = AssiyaAPI.getSnapshot(attacker),
            defenderSnapshot = AssiyaAPI.getSnapshot(target),
            attributes = attributes,
            clear = isClear.get(meta, ae).toBoolean(),
            overwrite = overwrite.get(meta, ae).toBoolean(),
            disabledAttributes = disableAttribute?.split(",") ?: emptyList()
        ) { pcb ->
            pcb[CustomDataComponent.Key]?.putAll(parsed)
            pcb += TriggerBySkillComponent(meta)
        }

        val realCaster = when {
            attacker.uniqueId == caster.uniqueId -> meta.caster

            else -> {
                MythicBukkit.inst().skillManager.getCaster(BukkitAdapter.adapt(attacker))
            }
        }

        val damageMetadata = DamageMetadataFactory.create(realCaster, meta)

        // 伤害逻辑
        val damageMeta = DamageMeta(processor).apply {
            ignoreImmunity = this@DamageMetaMechanic.ignoreImmunity.get(meta, ae).toBoolean()
            preventKnockback = this@DamageMetaMechanic.preventKnockback.get(meta, ae).toBoolean()
            noDamageTicks = this@DamageMetaMechanic.noDamageTicks.get(meta, ae).toIntOrNull()

            preDamage = {
                var isCancelled = false
                val damage = it.processor.getComponent(DamageMultipleComponent.Key).totalDamage

                damageMetadata.amount = damage
                damageMetadata.preventsImmunity = ignoreImmunity
                damageMetadata.preventsKnockback = preventKnockback
                ae.setMetadata("skill-damage", damageMetadata)

                val signal = signal.get(meta, ae)
                if (signal.isNotEmpty()) {
                    isCancelled = SignalUtils.publish(
                        ae, realCaster.entity, signal, hashMapOf(
                            "damage-amount" to Variable.ofType(VariableType.FLOAT, damage)
                        )
                    )
                }

                isCancelled
            }

            postDamage = {
                postAttackSkill?.let { skill ->
                    val copied = meta.deepClone()
                    copied.trigger = realCaster.entity
                    skill.execute(copied)
                }
            }
        }

        // 造成伤害
        try {
            realCaster.isUsingDamageSkill = true
            damageMeta.doDamage()
        } finally {
            realCaster.isUsingDamageSkill = false
            ae.removeMetadata("skill-damage")
        }

        return SkillResult.SUCCESS
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
            "disableAttribute", "da",
            "postAttackSkill", "postAttack", "pas"
        ).mapTo(hashSetOf()) { it.lowercase() }

    }

}
