package scripts.condition

import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import groovy.transform.CompileStatic
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

@CompileStatic
static boolean check(Condition condition, Entity entity, ConditionContextImpl context) {
    if (!(entity instanceof Player)) return true

    Player player = (Player) entity
    String permissions = context.capture('value')
    for (String permission in permissions.split('/')) {
        if (player.hasPermission(permission.trim())) return true
    }
    return false
}
