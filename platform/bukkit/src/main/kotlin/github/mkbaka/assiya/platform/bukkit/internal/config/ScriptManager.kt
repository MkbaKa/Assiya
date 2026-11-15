package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.internal.config.loader.FileLoader
import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy.GroovyEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptScheduler
import github.mkbaka.assiya.platform.bukkit.internal.util.warning
import github.mkbaka.bukkitksp.annotation.CustomTask
import java.util.concurrent.ConcurrentHashMap

object ScriptManager {

    private val engines = ConcurrentHashMap<String, ScriptEngineAdapter>()
    private val scripts = ConcurrentHashMap<String, Script>()

    val size: Int
        get() = scripts.size

    init {
        registerEngine(NashornEngineAdapter, "js")
        registerEngine(GroovyEngineAdapter, "groovy")
    }

    fun registerEngine(engine: ScriptEngineAdapter, vararg extensions: String) {
        for (extension in extensions) {
            engines.putIfAbsent(extension.lowercase(), engine)
        }
    }

    fun containsScript(script: String): Boolean {
        return scripts.containsKey(script.lowercase())
    }

    fun invoke(id: String, function: String, vararg args: Any?): Any? {
        return scripts[id.lowercase()]?.invokeSafely(function, *args)
    }

    operator fun get(id: String): Script? = scripts[id.lowercase()]

    fun unload() {
        ScriptScheduler.unregister()
        ScriptListener.unregister()
    }

    @CustomTask(taskId = "reload", priority = 5)
    fun reload() {
        invokeAll("onDisable")
        unload()
        scripts.clear()

        FileLoader.load("scripts") { file ->
            val engine = engines[file.extension.lowercase()] ?: return@load
            val relativePath = file.invariantSeparatorsPath.substringAfterLast("scripts/")
            val id = relativePath.substringBeforeLast('.')
            try {
                val script = engine.compile(id, file)
                if (scripts.putIfAbsent(id.lowercase(), script) != null) {
                    warning("Duplicate key $id found in scripts")
                }
            } catch (e: Throwable) {
                warning("Failed to load scripts file $relativePath: ${e.message}")
            }
        }
        invokeAll("onEnable")
    }

    private fun invokeAll(function: String) {
        for (script in scripts.values) {
            if (script.hasFunction(function)) {
                script.invokeSafely(function)
            }
        }
    }

}
