package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.HandlerComponent
import java.util.concurrent.atomic.AtomicBoolean

object AttributeRegistration {

    private var batchDepth = 0
    private var dirty = false

    private val refreshEnabled = AtomicBoolean(false)

    /**
     * 见 [HandlerRegistry]#index
     */
    fun begin() {
        refreshEnabled.set(false)
    }

    fun complete() {
        try {
            AttributeRegistry.refresh()
        } finally {
            refreshEnabled.set(true)
        }
    }

    fun register(attribute: Attribute) {
        AttributeRegistry.register(attribute)
        val triggers = attribute[HandlerComponent]
        try {
            if (triggers != null) {
                HandlerRegistry.register(attribute.id, triggers)
            }
            changed()
        } catch (cause: Throwable) {
            if (triggers != null) {
                HandlerRegistry.unregister(attribute.id, triggers)
            }
            AttributeRegistry.unregister(attribute)
            throw cause
        }
    }

    fun unregister(attribute: Attribute) {
        if (AttributeRegistry[attribute.id] !== attribute) {
            return
        }

        val triggers = attribute[HandlerComponent]
        if (triggers != null) {
            HandlerRegistry.unregister(attribute.id, triggers)
        }
        AttributeRegistry.unregister(attribute)
        changed()
    }

    fun <T> batch(action: () -> T): T {
        beginBatch()
        return try {
            action()
        } finally {
            endBatch()
        }
    }

    private fun changed() {
        if (!refreshEnabled.get()) return
        if (batchDepth == 0) {
            AttributeRegistry.refresh()
        } else {
            dirty = true
        }
    }

    private fun beginBatch() {
        if (batchDepth++ == 0) {
            HandlerRegistry.begin()
        }
    }

    private fun endBatch() {
        check(batchDepth > 0) { "Attribute registration batch is not active" }
        if (--batchDepth != 0) {
            return
        }

        try {
            HandlerRegistry.complete()
            if (dirty && refreshEnabled.get()) {
                AttributeRegistry.refresh()
            }
        } finally {
            dirty = false
        }
    }
}
