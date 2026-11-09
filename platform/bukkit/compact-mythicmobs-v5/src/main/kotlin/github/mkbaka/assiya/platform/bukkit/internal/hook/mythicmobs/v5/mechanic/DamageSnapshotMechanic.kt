package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.mechanic

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.util.Utils
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import org.bukkit.entity.LivingEntity

@MythicMechanic("assiya:damage-snapshot", "assiya:damagesnapshot", "assiya:ds")
class DamageSnapshotMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : SkillMechanic(container.manager, container.file, mlc.line, mlc), ITargetedEntitySkill {

    private val id = mlc.getPlaceholderString(arrayOf("id", "i"), "")
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

    private val variables = mlc.entrySet().asSequence()
        .filter { it.key !in IGNORE }
        .associate { it.key to PlaceholderString.of(it.value) }

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val target = ae.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET

        val id = id.get(meta, ae)
        if (id.isEmpty()) return SkillResult.INVALID_CONFIG

        val selected = Utils.selectDamageSubscribers(usableAttribute)

        if (selected != null && selected.isEmpty()) return SkillResult.INVALID_CONFIG

        val parsed = Utils.parseAllEntries(variables, meta, ae)
        val overwrite = overwrite.get(meta, ae).toBoolean()
        val attackerSnapshot = Utils.buildDamageAttackerSnapshot(
            target,
            parsed,
            isClear.get(meta, ae).toBoolean(),
            overwrite
        )
        val disabledIds = Utils.resolveAttributeIds(disableAttribute)

        val processor = DamageProcessor.ofSnapshot(
            attacker = target,
            defender = target,
            subscribers = selected,
            attackerSnapshot = attackerSnapshot
        ) { pcb ->
            pcb[CustomDataComponent]?.putAll(parsed)
            Utils.disableAttributes(pcb, disabledIds)
        }

        AssiyaAPI.getSessionOrCreate(target).metadata[metadataKey(id)] = processor

        return SkillResult.SUCCESS
    }

    companion object {

        private const val METADATA_PREFIX = "assiya:damage-snapshot:"

        private val IGNORE = arrayOf(
            "id", "i",
            "clear", "c", "isClear",
            "overwrite", "ow",
            "usableAttribute", "ua",
            "disableAttribute", "da"
        ).mapTo(hashSetOf()) { it.lowercase() }

        @JvmStatic
        fun metadataKey(id: String): String = "$METADATA_PREFIX$id"

    }

}

