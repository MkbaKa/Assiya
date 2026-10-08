package github.mkbaka.assiya.platform.bukkit.internal.hook.placeholderapi

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
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
                        argument("default", StringArgument<OfflinePlayer, String>().setDefaultValue("")) {
                            // TODO
                            setExecutor { context ->
                                val default = context.getArgument<String>("default")
                                val player = context.source ?: return@setExecutor default
                                val session = AssiyaAPI.getSession(player.uniqueId)
                                    ?: return@setExecutor default
                                val attribute = context.getArgument<String>("attr")
                                val capture = context.getArgument<String>("capture")

                                val snapshot = session.attribute.snapshot
                                val attr = AttributeRegistry[attribute] ?: return@setExecutor default
                                val status = snapshot[attribute] ?: return@setExecutor default
                                val result = attr[ReaderComponent.Key]?.reader?.read(status, capture)

                                return@setExecutor result?.toString() ?: default
                            }
                        }
                    }
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
            else node.execute(StringReader.of(params, '_', '\\'), player) { "" } ?: ""
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