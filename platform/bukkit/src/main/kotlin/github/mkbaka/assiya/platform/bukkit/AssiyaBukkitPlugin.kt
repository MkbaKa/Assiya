package github.mkbaka.assiya.platform.bukkit

import github.mkbaka.assiya.platform.bukkit.internal.AwakeLifecycle
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.colored
import org.bukkit.Bukkit
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
        MessageType.PLUGIN_ENABLE.sendTo(
            Bukkit.getConsoleSender(),
            mapOf("server-version" to Bukkit.getBukkitVersion())
        )
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

        val PLUGIN_NAME: String by lazy {
            "&9&lAssi&b&lya".colored()
        }

        val PREFIX: String by lazy { "&8[$PLUGIN_NAME&8]".colored() }

    }

}
