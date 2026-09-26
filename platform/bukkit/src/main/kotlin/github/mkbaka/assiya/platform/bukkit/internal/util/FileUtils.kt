package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

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

    fun loadYamlOrNull(directoryName: String, file: File, relativePath: String): YamlConfiguration? {
        try {
            return YamlConfiguration.loadConfiguration(file)
        } catch (e: InvalidConfigurationException) {
            warning("$directoryName $relativePath contains a syntax error: ${e.message}")
            return null
        } catch (e: IOException) {
            warning("I/O error while reading $directoryName $relativePath: ${e.message}")
            return null
        }
    }

    fun saveIfNotExists(resource: String): File {
        val file = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, resource)
        if (!file.exists()) {
            AssiyaBukkitPlugin.INSTANCE.saveResource(resource, false)
        }
        return file
    }

}
