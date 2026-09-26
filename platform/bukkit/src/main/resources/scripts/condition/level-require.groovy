package scripts.condition

import github.mkbaka.assiya.common.condition.ConditionContext
import groovy.transform.CompileStatic
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

@CompileStatic
static boolean check(Entity entity, ConditionContext context) {
    // 若被判断的实体不是玩家
    if (!entity instanceof Player) return true

    Player player = entity as Player
    // 取 reader 解析后的值
    int require = context.capture("value")
    return player.level >= require
}
