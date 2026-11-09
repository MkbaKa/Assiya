package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.condition

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.api.skills.conditions.ISkillMetaComparisonCondition
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition
import io.lumine.mythic.core.skills.SkillCondition

@MythicCondition("assiya:check-damage-data", "assiya:check-data", "assiya:checkdamagedata", "assiya:checkdata")
class DamageDataCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition, ISkillMetaCondition, ISkillMetaComparisonCondition {

    private val key = mlc.getPlaceholderString(arrayOf("key", "k"), "")
    private val value = mlc.getPlaceholderString(arrayOf("value", "v"), "")
    private val useScript = mlc.getBoolean(arrayOf("useScript", "script", "us"), false)

    override fun check(ae: AbstractEntity): Boolean {
        return logic(ae)
    }

    override fun check(meta: SkillMetadata): Boolean {
        return logic(meta.caster.entity)
    }

    override fun check(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        return logic(ae)
    }

    private fun logic(ae: AbstractEntity): Boolean {
        val session = AssiyaAPI.getSession(ae.uniqueId) ?: return false
        val processor = session.damageProcessorStack.current ?: return false
        val key = key.get(ae)
        val value = value.get(ae)
        val data = processor[CustomDataComponent]?.get(key) ?: return false

        return if (useScript) {
            NashornEngineAdapter.eval("$data$value", emptyMap()) as? Boolean ?: false
        } else {
            data.toString() == value
        }
    }

}
