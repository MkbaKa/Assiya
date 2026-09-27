package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.internal.CustomTasks
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import java.util.concurrent.atomic.AtomicBoolean

internal object Config {

    private val reloading = AtomicBoolean(false)

    lateinit var values: ConfigNode
        private set

    @Awake(lifeCycle = LifeCycle.LOAD)
    fun load() {
        values = ConfigLoader.loadConfig("config.yml")
    }

    @Awake(lifeCycle = LifeCycle.ENABLE)
    fun reload(): Boolean {
        if (!reloading.compareAndSet(false, true)) {
            return false
        }

        try {
            values = ConfigLoader.loadConfig("config.yml")

            AttributeRegistration.begin()
            try {
                CustomTasks.run("reload")
            } finally {
                try {
                    AttributeRegistration.complete()
                } catch (e: Exception) {
                    logEx(e, "Failed to rebuild attribute parser after configuration reload")
                }
            }
        } finally {
            reloading.set(false)
        }
        return true
    }

}
