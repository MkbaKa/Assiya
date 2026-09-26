package github.mkbaka.assiya.platform.bukkit.internal.metadata

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.event.EventPriority
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object MetadataRegistry {

    private val metadata = ConcurrentHashMap<UUID, Metadata>()

    @JvmStatic
    fun setMetadata(uuid: UUID, key: String, value: Any) {
        metadata.computeIfAbsent(uuid) { Metadata(uuid) }.data[key] = value
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> getMetadata(uuid: UUID, key: String): T? {
        return metadata[uuid]?.data?.get(key) as? T
    }

    // TODO entity data move to session ?
    @Listener(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun remove(event: EntityRemoveFromWorldEvent) {
        metadata.remove(event.entity.uniqueId)
    }

    class Metadata(
        val owner: UUID,
        val data: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
    )

}