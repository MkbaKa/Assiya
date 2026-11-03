package scripts.condition

import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import groovy.transform.CompileStatic
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

@CompileStatic
static boolean check(Condition condition, Entity entity, ConditionContextImpl context) {
    // 若被判断的实体不是玩家
    if (!entity instanceof Player) return true

    Player player = entity as Player

    // min 和 max 可能是 null
    // 要用包装类型判断 不为 null 再转基本类型
    Number min = context.capture("min")
    Number max = context.capture("max")
    if (min != null && max != null) {
        return player.level >= min.intValue() && player.level <= max.intValue()
    }

    int require = context.capture("value")
    return player.level >= require
}
