package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.EntityProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import org.bukkit.entity.LivingEntity

interface TargetComponent : ProcessorComponent, EntityProcessorComponent, AttributeContainer {

    override val entity: LivingEntity

    override val key: ProcessorComponent.Key<*>
        get() = Key

    /**
     * 实现类可以复用的 key
     * 这样 processor 的调用点就不用穷举 n 种 Target 实现了
     * 比如 [TargetDefaultComponent] 和 [TargetLazyComponent]
     * 调用处只需要 processor[TargetComponent.Key]
     *
     * 但如果希望调用像 [TargetLazyComponent.set] 这种专有行为要强转一下
     * ```kotlin
     * (processor[TargetComponent.Key] as TargetLazyComponent).set(entity)
     * ```
     */
    companion object Key : ProcessorComponent.Key<TargetComponent>

}