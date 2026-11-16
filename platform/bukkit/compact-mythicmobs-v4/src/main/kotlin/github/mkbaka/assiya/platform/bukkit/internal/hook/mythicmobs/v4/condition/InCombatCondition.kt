package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.condition

import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillCondition
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition
import org.bukkit.entity.Player

@MythicCondition("assiya:in-combat", "assiya:incombat")
class InCombatCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    override fun check(ae: AbstractEntity): Boolean {
        val player = ae.bukkitEntity as? Player ?: return false
        return CombatStateAPI.inCombat(player)
    }

}
