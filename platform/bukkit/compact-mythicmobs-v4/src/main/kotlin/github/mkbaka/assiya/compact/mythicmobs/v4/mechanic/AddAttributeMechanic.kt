package github.mkbaka.assiya.compact.mythicmobs.v4.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v4.util.Utils
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeValue
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@MythicMechanic("assiya:add-attr", "assiya:addattr")
class AddAttributeMechanic(
    mlc: MythicLineConfig
) : SkillMechanic(mlc.line, mlc), ITargetedEntitySkill {

    private val source = mlc.getPlaceholderString(arrayOf("source", "s"), UUID.randomUUID().toString())
    private val duration = mlc.getPlaceholderString(arrayOf("duration", "d"), "-1")
    private val priority = mlc.getPlaceholderString(arrayOf("priority", "p"), "200")

    private val variables = mlc.entrySet().asSequence()
        .filter { it.key !in IGNORE }
        .associate { it.key to PlaceholderString.of(it.value) }

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        val target = ae.bukkitEntity

        val sourceId = source.get(meta, ae)
        val ticks = duration.get(meta, ae).toLong()
        val priority = priority.get(meta, ae).toInt()

        val parsed = Utils.parseAllEntries(variables, meta, ae)
        val attributes = Utils.parseAttributes(target, parsed)

        val values = arrayListOf<AttributeValue<*>>()
        for ((attributeId, status) in attributes) {
            val attribute = AttributeRegistry[attributeId] ?: error("Unknown attribute: $attributeId")
            val reader = attribute[ReaderComponent]?.reader ?: error("$attributeId has no reader")
            values += AttributeValue(attributeId, status, reader.capture)
        }

        val lifeCycle = if (ticks == -1L) {
            AttributeSourceLifecycle.Stable
        } else {
            AttributeSourceLifecycle.Temporary(50.milliseconds * ticks.toDouble())
        }

        val patch = AttributeSourcePatch(sourceId, priority, lifeCycle, values)
        AssiyaAPI.getSessionOrCreate(target).submitPatches(listOf(patch))
        return true
    }

    companion object {

        private val IGNORE = setOf("source", "s", "duration", "d", "priority", "p")

    }

}
