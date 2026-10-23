package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.event.util.CancellableEvent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * [DamageMeta] 执行过程中的各个阶段
 * Start ->
 *      [DamageMeta.process] == true ->
 *              PreProcess -> PostProcess ->
 *      PreDamage -> PostDamage ->
 *      End
 *
 * @param [damageMeta]
 * @param [processor]
 */
sealed class DamageMetaEvent(
    val damageMeta: DamageMeta,
    val processor: DamageProcessor
) : Event(!Bukkit.isPrimaryThread()) {

    class Start(damageMeta: DamageMeta, processor: DamageProcessor) : DamageMetaEvent(damageMeta, processor),
        Cancellable by CancellableEvent()

    class PreProcess(damageMeta: DamageMeta, processor: DamageProcessor) : DamageMetaEvent(damageMeta, processor),
        Cancellable by CancellableEvent()

    class PostProcess(damageMeta: DamageMeta, processor: DamageProcessor) : DamageMetaEvent(damageMeta, processor)

    class PreDamage(damageMeta: DamageMeta, processor: DamageProcessor) : DamageMetaEvent(damageMeta, processor),
        Cancellable by CancellableEvent()

    class PostDamage(damageMeta: DamageMeta, processor: DamageProcessor) : DamageMetaEvent(damageMeta, processor)

    class End(damageMeta: DamageMeta, processor: DamageProcessor) : DamageMetaEvent(damageMeta, processor)

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {

        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS

    }

}