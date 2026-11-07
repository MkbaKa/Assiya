package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.internal.CustomTasks
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadModule
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigGlobal
import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadResult
import github.mkbaka.assiya.platform.bukkit.internal.message.Messages
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
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
    fun reload(): Boolean = reloadAll() == ReloadResult.SUCCESS

    fun reloadAll(): ReloadResult {
        if (!reloading.compareAndSet(false, true)) {
            return ReloadResult.BUSY
        }

        try {
            load()

            AttributeRegistration.begin()
            try {
                CustomTasks.run("reload")
            } finally {
                AttributeRegistration.complete()
            }

            global.parseDefaultAttributes()
            AssiyaSessionRegistry.refresh()
            return ReloadResult.SUCCESS
        } catch (e: Exception) {
            logEx(e, "Failed to reload configuration")
            return ReloadResult.FAILED
        } finally {
            reloading.set(false)
        }
    }

    fun reloadModule(module: ReloadModule): ReloadResult {
        if (module == ReloadModule.ALL) {
            return reloadAll()
        }
        if (!reloading.compareAndSet(false, true)) {
            return ReloadResult.BUSY
        }

        try {
            return if (module.reload()) ReloadResult.SUCCESS else ReloadResult.FAILED
        } catch (e: Exception) {
            logEx(e, "Failed to reload module ${module.id}")
            return ReloadResult.FAILED
        } finally {
            reloading.set(false)
        }
    }

}
