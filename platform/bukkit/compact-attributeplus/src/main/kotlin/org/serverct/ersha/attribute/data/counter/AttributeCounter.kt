package org.serverct.ersha.attribute.data.counter

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.counter.CounterResetType
import org.bukkit.entity.Entity
import java.util.*

class AttributeCounter(private val entity: Entity) {
    fun getCounter(name: String, type: String): ItemCounter = ItemCounter(
        AssiyaAPI.getSessionOrCreate(entity, false).counter.getOrCreate(
            "attributeplus:counter:$name", CounterResetType.valueOf(type.uppercase(Locale.ROOT))
        )
    )

    fun getCounter(name: String): ItemCounter? = AssiyaAPI.getSession(entity)?.counter
        ?.get("attributeplus:counter:$name")?.let { ItemCounter(it) }

    fun containsCounter(name: String): Boolean =
        AssiyaAPI.getSession(entity)?.counter?.contains("attributeplus:counter:$name") == true

    fun removeCounter(name: String) {
        AssiyaAPI.getSession(entity)?.counter?.remove("attributeplus:counter:$name")
    }
}

