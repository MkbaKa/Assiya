package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.condition

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.core.skills.SkillCondition
import io.lumine.mythic.api.skills.conditions.IEntityCondition

@MythicCondition("assiya:has-attr-source", "assiya:hasattrsource")
class HasAttributeSourceCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    private val source = mlc.getPlaceholderString(arrayOf("source", "s"), "")

    override fun check(ae: AbstractEntity): Boolean {
        val sourceId = source.get(ae)
        return sourceId in AssiyaAPI.getSnapshot(ae.bukkitEntity).sources
    }

}

