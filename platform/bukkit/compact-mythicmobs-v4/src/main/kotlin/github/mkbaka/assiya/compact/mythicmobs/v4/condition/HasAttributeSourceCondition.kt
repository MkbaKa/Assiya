package github.mkbaka.assiya.compact.mythicmobs.v4.condition

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillCondition
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition

@MythicCondition("assiya:has-attr-source", "assiya:hasattrsource")
class HasAttributeSourceCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    private val source = mlc.getPlaceholderString(arrayOf("source", "s"), "")

    override fun check(ae: AbstractEntity): Boolean {
        val sourceId = source.get(ae)
        return sourceId in AssiyaAPI.getSnapshot(ae.bukkitEntity).sources
    }

}

