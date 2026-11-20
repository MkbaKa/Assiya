package github.mkbaka.assiya.platform.bukkit.internal.config.loader

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.config.BukkitConfigNode
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.api.config.DefaultConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.function.BiConsumer
import java.util.function.Consumer

object FileLoader {

    @JvmStatic
    @JvmOverloads
    fun loadConfig(
        path: String,
        resource: String = path,
        plugin: JavaPlugin = AssiyaBukkitPlugin.INSTANCE
    ): ConfigNode {
        val file = FileUtils.saveIfNotExists(path, resource, plugin)
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
    fun load(resourceDir: String, processor: Consumer<File>) {
        load(FileUtils.saveDirectoryIfNotExists(resourceDir), processor)
    }

    @JvmStatic
    fun load(target: File, processor: Consumer<File>) {
        val files = if (target.isDirectory) {
            target.walkTopDown().filter { it.isFile }
        } else {
            sequenceOf(target)
        }
        for (file in files) {
            processor.accept(file)
        }
    }

    @JvmStatic
    fun load(resourceDir: String, processor: BiConsumer<String, ConfigNode>) {
        load(FileUtils.saveDirectoryIfNotExists(resourceDir), processor)
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
        val loadedIds = hashSetOf<String>()

        load(target) { file ->
            if (file.extension != "yml" && file.extension != "yaml") return@load
            val root = try {
                BukkitConfigNode(FileUtils.readYaml(file))
            } catch (e: Exception) {
                logEx(e, "Failed to read configuration $file")
                return@load
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
