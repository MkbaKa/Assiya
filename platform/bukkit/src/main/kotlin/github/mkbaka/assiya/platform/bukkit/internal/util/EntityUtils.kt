package github.mkbaka.assiya.platform.bukkit.internal.util

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
        is String -> Bukkit.getPlayerExact(input)
        is UUID -> input.playerOrNull ?: input.entityOrNull

        else -> error("Unexpected entity type in $input")
    }
}