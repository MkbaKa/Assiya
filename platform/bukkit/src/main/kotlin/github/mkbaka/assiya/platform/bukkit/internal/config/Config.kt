package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.internal.CustomTasks
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigGlobal
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.assiya.platform.bukkit.internal.message.Messages
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import java.util.concurrent.atomic.AtomicBoolean

object Config {

    @Volatile
    lateinit var messages: Messages

    private val reloading = AtomicBoolean(false)

    @Volatile
    @JvmStatic
    lateinit var global: ConfigGlobal
        private set

    @Awake(lifeCycle = LifeCycle.LOAD)
    fun load() {
        global = ConfigGlobal(ConfigLoader.loadConfig("config.yml"))
        messages = Messages.load()
    }

    @Awake(lifeCycle = LifeCycle.ENABLE)
    fun reload(): Boolean {
        if (!reloading.compareAndSet(false, true)) {
            return false
        }

        try {
            load()

            AttributeRegistration.begin()
            try {
                CustomTasks.run("reload")
            } finally {
                try {
                    AttributeRegistration.complete()
                } catch (e: Exception) {
                    logEx(e, "Failed to rebuild attribute parser after configuration reload")
                    return false
                }
            }

            global.parseDefaultAttributes()
            AssiyaSessionRegistry.refresh()
        } finally {
            reloading.set(false)
        }
        return true
    }

}
