package github.mkbaka.assiya.platform.bukkit.internal.config.loader

import github.mkbaka.assiya.platform.bukkit.api.config.BukkitConfigNode
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.api.config.DefaultConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils.getSubFiles
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import java.io.File
import java.util.function.BiConsumer

object ConfigLoader {

    @JvmStatic
    @JvmOverloads
    fun loadConfig(path: String, resource: String = path): ConfigNode {
        val file = FileUtils.saveIfNotExists(path, resource)
        return BukkitConfigNode(FileUtils.readYaml(file))
    }

    @JvmStatic
    fun loadConfig(path: String, default: Map<String, *>): ConfigNode {
        val comments = linkedMapOf<String, List<String>>()
        val defaults = ConfigYaml.parse(default, comments)
        val file = FileUtils.saveIfNotExists(path) { ConfigYaml.render(defaults, comments) }
        return DefaultConfigNode(BukkitConfigNode(FileUtils.readYaml(file)), BukkitConfigNode(defaults))
    }

    @JvmStatic
    fun load(path: String, resources: List<String>, processor: BiConsumer<String, ConfigNode>) {
        val target = FileUtils.saveDirectoryIfNotExists(path, resources)
        load(target, processor)
    }

    @JvmStatic
    fun load(path: String, defaultFile: String, defaults: Map<String, *>, processor: BiConsumer<String, ConfigNode>) {
        val target = FileUtils.saveDirectoryIfNotExists(path, defaultFile) {
            val comments = linkedMapOf<String, List<String>>()
            val yaml = ConfigYaml.parse(defaults, comments)
            ConfigYaml.render(yaml, comments)
        }
        load(target, processor)
    }

    private fun load(target: File, processor: BiConsumer<String, ConfigNode>) {
        val files = if (target.isDirectory) target.getSubFiles() else listOf(target)
        val loadedIds = hashSetOf<String>()

        for (file in files) {
            if (file.extension != "yml" && file.extension != "yaml") continue
            val root = try {
                BukkitConfigNode(FileUtils.readYaml(file))
            } catch (e: Exception) {
                logEx(e, "Failed to read configuration $file")
                continue
            }
            for (id in root.keys()) {
                try {
                    check(id !in loadedIds) { "Duplicate configuration id: $id" }
                    processor.accept(id, root)
                    loadedIds += id
                } catch (e: Exception) {
                    logEx(e, "Failed to load configuration $file, key $id")
                }
            }
        }
    }

}
