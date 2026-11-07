package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.isUUIDEZ
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*

val UUID.playerOrNull: Player?
    get() = Bukkit.getPlayer(this)

val UUID.player: Player
    get() = playerOrNull!!

val UUID.entityOrNull: Entity?
    get() = Bukkit.getEntity(this)

val UUID.entity: Entity
    get() = entityOrNull!!

fun entityOf(input: Any): Entity? {
    return when (input) {
        is Entity -> input
        is String -> {
            if (input.isUUIDEZ()) {
                val uuid = try {
                    UUID.fromString(input)
                } catch (_: IllegalArgumentException) {
                    return Bukkit.getPlayerExact(input)
                }
                uuid.playerOrNull ?: uuid.entityOrNull
            } else {
                Bukkit.getPlayerExact(input)
            }
        }

        is UUID -> input.playerOrNull ?: input.entityOrNull

        else -> error("Unexpected entity type in $input")
    }
}
