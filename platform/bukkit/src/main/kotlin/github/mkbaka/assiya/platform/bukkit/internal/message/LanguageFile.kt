package github.mkbaka.assiya.platform.bukkit.internal.message

import github.mkbaka.assiya.platform.bukkit.api.config.BukkitConfigNode
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

internal object LanguageFile {

    fun load(path: String): ConfigNode {
        val file = FileUtils.saveIfNotExists(path)
        val yaml = FileUtils.readYaml(file)
        val defaults = readDefaults(path) ?: return BukkitConfigNode(yaml)
        update(file, yaml, defaults)
        return BukkitConfigNode(yaml)
    }

    private fun readDefaults(path: String): YamlConfiguration? {
        val resource = AssiyaBukkitPlugin.INSTANCE.getResource(path) ?: return null
        return YamlConfiguration().apply {
            resource.bufferedReader(Charsets.UTF_8).use { loadFromString(it.readText()) }
        }
    }

    private fun update(file: File, yaml: YamlConfiguration, defaults: YamlConfiguration) {
        val additions = YamlConfiguration()
        for (key in defaults.getKeys(false)) {
            if (!yaml.contains(key)) {
                additions.set(key, defaults.get(key))
            }
        }
        if (additions.getKeys(false).isEmpty()) return

        val original = file.readText(Charsets.UTF_8)
        val date = SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date())
        val updated = "$original\n# UPDATE $date\n${additions.saveToString()}"
        file.writeText(updated, Charsets.UTF_8)
    }

}
