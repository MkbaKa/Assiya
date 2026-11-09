package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.condition

import github.mkbaka.mythicksp.annotation.MythicConditionVariant
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.conditions.ISkillMetaComparisonCondition

@MythicConditionVariant(
    base = DamageDataCondition::class,
    // MMX 新增
    requiresClass = "io.lumine.xikage.mythicmobs.skills.conditions.ISkillMetaComparisonCondition"
)
class DamageDataComparisonCondition(mlc: MythicLineConfig) :
    DamageDataCondition(mlc), ISkillMetaComparisonCondition {

    override fun check(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        return logic(ae)
    }

}
