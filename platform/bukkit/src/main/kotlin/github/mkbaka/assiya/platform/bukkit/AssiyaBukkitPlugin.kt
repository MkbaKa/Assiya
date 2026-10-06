package github.mkbaka.assiya.platform.bukkit

import github.mkbaka.assiya.platform.bukkit.internal.AwakeLifecycle
import org.bukkit.plugin.java.JavaPlugin

/**
 * TODO 抽离公告逻辑去 common 模块
 *      common 只留接口 具体实现拆分 common-impl 模块
 */
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
