package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.common.source.AttributeSourceInfo
import github.mkbaka.assiya.platform.bukkit.api.event.util.CancellableEvent
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 当一个属性源被移除时触发
 * [Pre] 取消后会阻止本次移除行为 像过期的临时源会等到下次其他源一起处理
 */
sealed class AttributeSourceRemoveEvent : Event(!Bukkit.isPrimaryThread()) {

    class Pre(val session: AssiyaSession, val info: AttributeSourceInfo) : AttributeSourceRemoveEvent(),
        Cancellable by CancellableEvent()

    class Post(val session: AssiyaSession, val info: AttributeSourceInfo) : AttributeSourceRemoveEvent()

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {

        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS

    }

}
