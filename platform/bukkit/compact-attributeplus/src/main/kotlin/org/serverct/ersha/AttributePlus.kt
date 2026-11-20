package org.serverct.ersha

import github.mkbaka.assiya.platform.bukkit.internal.config.ScriptManager
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.FileLoader
import org.bukkit.plugin.java.JavaPlugin
import org.serverct.ersha.script.APScriptLoader
import java.io.File

class AttributePlus : JavaPlugin() {
    override fun onLoad() {
        instance = this
        FileLoader.loadConfig("script.yml", plugin = this)
        check(ScriptManager.registerDirectoryHandler(File(dataFolder, "script"), APScriptLoader))
    }

    fun loadScriptTools(): Map<String, String> {
        val configuration = FileLoader.loadConfig("script.yml", plugin = this)
        val section = configuration.getNode("scriptTools") ?: return emptyMap()
        val tools = linkedMapOf<String, String>()
        for (name in section.keys()) {
            tools[name] = requireNotNull(section.getString(name)) { "Missing class name for script tool $name" }
        }
        return tools
    }

    override fun onDisable() {
        APScriptLoader.unload()
    }

    companion object {
        lateinit var instance: AttributePlus
            private set
    }
}
