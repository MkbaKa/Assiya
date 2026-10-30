package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent

class ProjectileComponent(
    val shooter: TriggerComponent,
    val projectile: TriggerComponent,
    val force: Float
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<ProjectileComponent>

}
