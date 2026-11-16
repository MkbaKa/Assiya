package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.condition

import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player

@MythicCondition("assiya:in-combat", "assiya:incombat")
class InCombatCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    override fun check(ae: AbstractEntity): Boolean {
        val player = ae.bukkitEntity as? Player ?: return false
        return CombatStateAPI.inCombat(player)
    }

}
