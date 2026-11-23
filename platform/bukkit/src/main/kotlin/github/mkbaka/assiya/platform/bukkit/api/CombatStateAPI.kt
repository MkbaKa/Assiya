package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object CombatStateAPI {

    @JvmStatic
    @JvmOverloads
    fun join(player: Player, target: Entity? = null, durationTicks: Long = Config.global.COMBAT_DURATION) {
        AssiyaAPI.getSessionOrCreate(player, withAttribute = false)
            .combatState?.join(target, durationTicks)
    }

    @JvmStatic
    fun leave(player: Player) {
        AssiyaAPI.getSession(player)
            ?.combatState?.leave()
    }

    @JvmStatic
    fun inCombat(player: Player) = AssiyaAPI.getSession(player)?.combatState?.inCombat ?: false

}