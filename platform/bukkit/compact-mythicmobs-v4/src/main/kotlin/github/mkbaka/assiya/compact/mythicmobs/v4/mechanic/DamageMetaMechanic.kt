package github.mkbaka.assiya.compact.mythicmobs.v4.mechanic

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.compact.mythicmobs.v4.handler.component.TriggerBySkillComponent
import github.mkbaka.assiya.compact.mythicmobs.v4.util.SignalUtils
import github.mkbaka.assiya.compact.mythicmobs.v4.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeSnapshotBuilder
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.entityOf
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.mobs.GenericCaster
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.Skill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString
import io.lumine.xikage.mythicmobs.skills.variables.Variable
import io.lumine.xikage.mythicmobs.skills.variables.VariableType
import org.bukkit.entity.LivingEntity
import kotlin.jvm.optionals.getOrNull

@MythicMechanic("assiya:damage-meta", "assiya:damagemeta")
class DamageMetaMechanic(
    mlc: MythicLineConfig
) : SkillMechanic(mlc.line, mlc), ITargetedEntitySkill {

    /**
     * - assiya:damage-meta{physical-attack|min=100;physical-attack|max=200} @EIR{r=10}
     */

    private val attacker = mlc.getPlaceholderString(arrayOf("attacker", "player", "from", "source"), "")
    private val preventKnockback = mlc.getPlaceholderString(arrayOf("preventKnockback", "pkb", "pk"), "false")
    private val ignoreImmunity = mlc.getPlaceholderString(arrayOf("ignoreImmunity", "im", "pi", "ii"), "false")
    private val noDamageTicks = mlc.getPlaceholderString(arrayOf("noDamageTicks", "ndt"), "20")
    private val isClear = mlc.getPlaceholderString(arrayOf("clear", "c", "isClear"), "false")
    private val signal = mlc.getPlaceholderString(arrayOf("signal", "s"), "")
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

    private var postAttackSkill: Skill? = null

    private val variables = mlc.entrySet().asSequence()
        .filter { it.key !in IGNORE }
        .associate { it.key to PlaceholderString.of(it.value) }

    init {
        val skillManager = MythicMobs.inst().skillManager
        val postAttack = mlc.getString(arrayOf("pas", "postAttack", "postAttackSkill"), "")

        if (postAttack.isNotEmpty()) {
            skillManager.queueAfterLoad {
                postAttackSkill = skillManager.getSkill(postAttack).getOrNull()
            }
        }

        ASYNC_SAFE = false
    }

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        val target = ae.bukkitEntity as? LivingEntity ?: return false

        val usableIds = Utils.resolveAttributeIds(usableAttribute)
        val selected = if (usableAttribute == null) {
            null
        } else {
            HandlerRegistry.select("damage") { subscriber ->
                subscriber.attributeId in usableIds
            }
        }

        // 指定了可用属性但没有对应 handler 时不执行后续流程
        if (selected != null && selected.isEmpty()) return false

        val disabledIds = Utils.resolveAttributeIds(disableAttribute)

        val caster = meta.caster.entity.bukkitEntity

        // 找不到对应攻击者则默认施法者
        val attacker = entityOf(attacker.get(meta, ae)) ?: caster

        // 解析参数和属性
        val parsed = Utils.parseAllEntries(variables, meta, ae)
        val attributes = Utils.parseAttributes(attacker, parsed)

        val baseSnapshot = if (isClear.get(meta, ae).toBoolean()) {
            AttributeEngineSnapshot.EMPTY
        } else {
            AssiyaAPI.getSnapshot(attacker)
        }
        val snapshotBuilder = AttributeSnapshotBuilder(baseSnapshot)
        val overwrite = overwrite.get(meta, ae).toBoolean()
        for ((id, status) in attributes) {
            if (overwrite) {
                snapshotBuilder.setAttribute(id, status)
            } else {
                snapshotBuilder.addAttribute(id, status)
            }
        }

        // 上下文
        val processor = DamageProcessor.ofSnapshot(
            attacker = attacker,
            defender = target,
            subscribers = selected,
            attackerSnapshot = snapshotBuilder.build()
        ) { pcb ->
            for (id in disabledIds) {
                pcb[CancellableAttributeComponent]!!.setCancelled(id)
            }
            pcb[CustomDataComponent.Key]?.putAll(parsed)
            pcb += TriggerBySkillComponent(meta)
        }

        val realCaster = when {
            attacker.uniqueId == caster.uniqueId -> meta.caster

            else -> {
                getPlugin().mobManager.getActiveMob(attacker.uniqueId)
                    .getOrNull() ?: GenericCaster(BukkitAdapter.adapt(attacker))
            }
        }

        // 伤害逻辑
        val damageMeta = DamageMeta(processor).apply {
            ignoreImmunity = this@DamageMetaMechanic.ignoreImmunity.get(meta, ae).toBoolean()
            preventKnockback = this@DamageMetaMechanic.preventKnockback.get(meta, ae).toBoolean()
            noDamageTicks = this@DamageMetaMechanic.noDamageTicks.get(meta, ae).toIntOrNull()

            preDamage = {
                var isCancelled = false
                val damage = it.processor.getComponent(DamageMultipleComponent.Key).totalDamage

                val signal = signal.get(meta, ae)
                if (signal.isNotEmpty()) {
                    isCancelled = SignalUtils.publish(
                        ae, realCaster.entity, signal, hashMapOf(
                            "damage-amount" to Variable.ofType(VariableType.FLOAT, damage)
                        )
                    )
                }

                if (!isCancelled && realCaster is ActiveMob) {
                    realCaster.lastDamageSkillAmount = damage
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
        }

        return true
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
