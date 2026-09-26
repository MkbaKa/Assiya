package github.mkbaka.assiya.platform.bukkit

import github.mkbaka.assiya.platform.bukkit.internal.AwakeLifecycle
import org.bukkit.plugin.java.JavaPlugin

class AssiyaBukkitPlugin : JavaPlugin() {

    override fun onLoad() {
        AwakeLifecycle.load()
    }

    override fun onEnable() {
        AwakeLifecycle.enable()
    }

    override fun onDisable() {
        AwakeLifecycle.disable()
    }

    init {
        INSTANCE = this
    }

    companion object {

        lateinit var INSTANCE: AssiyaBukkitPlugin

    }

}
