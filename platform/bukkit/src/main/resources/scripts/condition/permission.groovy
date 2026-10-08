package scripts.condition

import github.mkbaka.assiya.common.condition.ConditionContext
import groovy.transform.CompileStatic
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

@CompileStatic
static boolean check(Entity entity, ConditionContext context) {
    if (!(entity instanceof Player)) return true

    Player player = (Player) entity
    String permissions = context.capture('value')
    for (String permission in permissions.split('/')) {
        if (player.hasPermission(permission.trim())) return true
    }
    return false
}
