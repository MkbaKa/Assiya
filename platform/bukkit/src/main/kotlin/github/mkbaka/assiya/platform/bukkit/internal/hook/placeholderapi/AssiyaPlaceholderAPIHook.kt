package github.mkbaka.assiya.platform.bukkit.internal.hook.placeholderapi

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.format
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import pers.neige.colonel.argument
import pers.neige.colonel.arguments.impl.StringArgument
import pers.neige.colonel.literal
import pers.neige.colonel.reader.StringReader
import pers.neige.colonel.root

object AssiyaPlaceholderAPIHook {

    private var hooked = false

    private val expansion by lazy {
        val node = root<OfflinePlayer, String>("assiya") {
            // %assiya_attr_<name>_<capture>_[<default>]%
            literal("attr") {
                argument("attr", StringArgument()) {
                    argument("capture", StringArgument()) {
                        argument("default", StringArgument<OfflinePlayer, String>().setDefaultValue(null)) {
                            setExecutor { context ->
                                val default = context.getArgument<String?>("default")
                                val player = context.source ?: return@setExecutor ""
                                val attribute = context.getArgument<String>("attr")
                                val capture = context.getArgument<String>("capture")

                                // 若属性不存着或者没有 reader 返回默认值
                                val attr = AssiyaAPI.getAttribute(attribute) ?: return@setExecutor default ?: ""
                                val reader = attr[ReaderComponent]?.reader
                                    ?: return@setExecutor default ?: ""

                                // 玩家没有这条属性 返回默认值
                                val status = AssiyaAPI.getSession(player.uniqueId)?.attribute?.snapshot?.get(attr.id)
                                if (status == null && default != null) return@setExecutor default

                                // 如果 capture 读到的是 null 则使用 default
                                // 如果没指定 default 就使用属性值类型的 默认值
                                // 比如数字类型是 0.0   文本是空字符串 ""
                                val readStatus = status ?: Status.empty<Any>()
                                val result = reader.read(readStatus, capture)
                                    ?: default
                                    ?: reader.defaultValue(capture)

                                return@setExecutor when (result) {
                                    is Number -> result.format(2)
                                    else -> result?.toString() ?: ""
                                }
                            }
                        }
                    }
                }
            }

            literal("in-combat") {
                setExecutor { context ->
                    val player = context.source ?: return@setExecutor "false"
                    AssiyaAPI.getSession(player.uniqueId)
                        ?.combatState?.inCombat?.toString()
                        ?: "false"
                }
            }
        }

        val desc = AssiyaBukkitPlugin.INSTANCE.description
        PlaceholderExpansionWrapper(
            "assiya",
            desc.authors.joinToString(", "),
            desc.version
        ) { player, params ->
            if (player == null) ""
            else node.execute(StringReader.of(params, '_', '\\'), player) { "Invalid action: $params" } ?: ""
        }
    }

    @Awake(lifeCycle = LifeCycle.ENABLE)
    fun enable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hooked = true
            expansion.register()
        }
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    fun disable() {
        if (hooked) {
            expansion.unregister()
        }
    }

}
