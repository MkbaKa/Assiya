package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.function.Supplier
import java.util.jar.JarFile

object FileUtils {

    @JvmStatic
    fun File.getSubFiles(): Sequence<File> {
        return walk().filter { it.isFile }
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

    fun saveDirectoryIfNotExists(path: String): File {
        val directory = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, path)
        if (directory.exists()) return directory

        val source = File(
            AssiyaBukkitPlugin::class.java.protectionDomain.codeSource.location.toURI()
        )
        val prefix = "$path/"
        val resources = JarFile(source).use { jar ->
            jar.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith(prefix) }
                .map { it.name }
                .toList()
        }

        directory.mkdirs()
        for (resource in resources) {
            saveIfNotExists(resource)
        }
        return directory
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
