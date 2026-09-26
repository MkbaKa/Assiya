package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSession
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSessionRegistry
import org.bukkit.entity.Entity

interface EntityProcessorComponent {

    val entity: Entity

    val snapshot: AttributeEngineSnapshot

    val session: AttributeSession?
        get() = AttributeSessionRegistry[entity.uniqueId]

}
