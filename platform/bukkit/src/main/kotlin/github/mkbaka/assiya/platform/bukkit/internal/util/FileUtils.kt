package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.function.Supplier

object FileUtils {

    @JvmStatic
    fun File.getSubFiles(): List<File> {
        val files = listFiles() ?: return emptyList()

        val result = ArrayList<File>()
        for (file in files) {
            if (file.isDirectory) {
                result += file.getSubFiles()
            } else {
                result += file
            }
        }

        return result
    }

    fun saveIfNotExists(resource: String): File {
        val file = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, resource)
        if (!file.exists()) {
            AssiyaBukkitPlugin.INSTANCE.saveResource(resource, false)
        }
        return file
    }

    fun readYaml(file: File): YamlConfiguration {
        val yaml = YamlConfiguration()
        yaml.load(file)
        return yaml
    }

    fun readResource(resource: String): String {
        return AssiyaBukkitPlugin.INSTANCE.getResource(resource)!!
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun saveIfNotExists(path: String, content: Supplier<String>): File {
        val file = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, path)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.writeText(content.get(), Charsets.UTF_8)
        }
        return file
    }

    fun saveIfNotExists(path: String, resource: String): File {
        if (path == resource) return saveIfNotExists(resource)
        return saveIfNotExists(path) { readResource(resource) }
    }

    fun saveDirectoryIfNotExists(path: String, resources: List<String>): File {
        val directory = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, path)
        if (!directory.exists()) {
            if (path !in resources) directory.mkdirs()
            for (resource in resources) saveIfNotExists(resource)
        }
        return directory
    }

    fun saveDirectoryIfNotExists(path: String, defaultFile: String, content: Supplier<String>): File {
        val directory = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, path)
        if (!directory.exists()) {
            directory.mkdirs()
            saveIfNotExists("$path/$defaultFile", content)
        }
        return directory
    }

}
