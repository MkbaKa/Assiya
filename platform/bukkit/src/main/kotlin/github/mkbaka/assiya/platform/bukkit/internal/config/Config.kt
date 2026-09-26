package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.CustomTasks
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import java.util.concurrent.atomic.AtomicBoolean

internal object Config {

    private val reloading = AtomicBoolean(false)

    // TODO onLoad save default
    @Awake(lifeCycle = LifeCycle.LOAD)
    fun load() {
        AssiyaBukkitPlugin.INSTANCE.saveDefaultConfig()
    }

    @Awake(lifeCycle = LifeCycle.ENABLE)
    fun reload(): Boolean {
        if (!reloading.compareAndSet(false, true)) {
            return false
        }

        try {
            AssiyaBukkitPlugin.INSTANCE.reloadConfig()

            CustomTasks.run("reload")
        } finally {
            reloading.set(false)
        }
        return true
    }

}
