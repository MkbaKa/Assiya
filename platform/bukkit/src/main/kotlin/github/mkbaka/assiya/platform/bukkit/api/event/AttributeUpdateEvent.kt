package github.mkbaka.assiya.platform.bukkit.api.event

import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.platform.bukkit.api.event.util.CancellableEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 由一次允许触发事件的 refresh 触发
 * [Pre] 取消后这次 refresh 不会有任何效果
 * [Post] 里修改 context 内容同样无效
 */
sealed class AttributeUpdateEvent : Event(!Bukkit.isPrimaryThread()) {

    class Pre(val entity: Entity, val context: AttributeUpdateContext) : AttributeUpdateEvent(),
        Cancellable by CancellableEvent()

    class Post(val entity: Entity, val context: AttributeUpdateContext) : AttributeUpdateEvent()

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {

        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS

    }

}