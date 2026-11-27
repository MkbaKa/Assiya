package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

sealed class SchedulerContext : AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<SchedulerContext>

}

class EntityContext(val entity: Entity) : SchedulerContext()

class RegionContext(location: Location) : SchedulerContext() {

    val worldName: String = location.world.name
    val world: World get() = requireNotNull(Bukkit.getWorld(worldName)) { "Region location must have a world" }

    val chunkX = location.blockX shr 4
    val chunkZ = location.blockZ shr 4

}
