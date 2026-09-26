package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.config.BukkitConfigNode
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils.getSubFiles
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils.loadYamlOrNull
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.assiya.platform.bukkit.internal.util.warning
import java.io.File

// TODO refactor
abstract class AbstractConfigManager<R : Any, T : Any>(
    private val configPath: String
) {

    private val configs = HashMap<String, T>()

    val size: Int
        get() = configs.size

    protected val values: Collection<T>
        get() = configs.values

    abstract fun readConfig(node: ConfigNode, key: String): R?

    abstract fun convert(id: String, value: R): T

    protected operator fun get(id: String): T? = configs[id]

    protected open fun defaultResources(): List<String> = emptyList()

    open fun reload() {
        configs.clear()

        val configFile = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, configPath)
        if (!configFile.exists()) {
            for (resource in defaultResources()) {
                AssiyaBukkitPlugin.INSTANCE.saveResource(resource, false)
            }
        }

        val files = if (configFile.isDirectory) configFile.getSubFiles() else listOf(configFile)
        for (file in files) {
            if (file.extension != "yml") continue
            val relativePath = if (configFile.isDirectory) {
                file.invariantSeparatorsPath.substringAfterLast("${configFile.name}/")
            } else {
                file.name
            }
            loadFile(file, relativePath)
        }
    }

    protected fun loadFile(file: File, relativePath: String) {
        val yaml = loadYamlOrNull(configPath, file, relativePath) ?: return
        val node = BukkitConfigNode(yaml)

        for (key in yaml.getKeys(false)) {
            if (configs.containsKey(key)) {
                warning("Duplicate key $key found in $configPath")
                continue
            }
            val config = readConfig(node, key)
            if (config == null) {
                warning("Invalid value for key $key in $configPath $relativePath")
                continue
            }
            val value = try {
                convert(key, config)
            } catch (e: Exception) {
                logEx(e, "Failed to process key $key in $configPath $relativePath")
                continue
            }
            configs[key] = value
        }
    }
}
