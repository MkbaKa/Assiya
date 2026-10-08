package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.internal.session.AssiyaSession
import org.bukkit.entity.Entity

interface EntityProcessorComponent {

    val entity: Entity

    val snapshot: AttributeEngineSnapshot

    val session: AssiyaSession?
        get() = AssiyaAPI.getSession(entity)

}
