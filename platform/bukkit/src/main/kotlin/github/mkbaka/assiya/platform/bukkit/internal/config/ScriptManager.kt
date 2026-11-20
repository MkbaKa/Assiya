package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.internal.config.loader.FileLoader
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptDirectoryHandler
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy.GroovyEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptScheduler
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.assiya.platform.bukkit.internal.util.warning
import github.mkbaka.bukkitksp.annotation.CustomTask
import org.bukkit.Bukkit
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object ScriptManager {

    private val engines = ConcurrentHashMap<String, ScriptEngineAdapter>()
    private val externalDirectories = ConcurrentHashMap<File, ScriptDirectoryHandler>()
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

    @JvmStatic
    fun registerDirectoryHandler(directory: File, handler: ScriptDirectoryHandler): Boolean {
        val path = directory.absoluteFile.normalize()
        if (externalDirectories.putIfAbsent(path, handler) != null) {
            warning("Duplicate script directory handler: $path")
            return false
        }
        return true
    }

    fun invoke(id: String, function: String, vararg args: Any?): Any? {
        return scripts[id.lowercase()]?.invokeSafely(function, *args)
    }

    operator fun get(id: String): Script? = scripts[id.lowercase()]

    fun unload() {
        ScriptScheduler.unregister()
        ScriptListener.unregister()
    }

    fun unloadHandlerDirectories() {
        for ((directory, handler) in externalDirectories) {
            try {
                handler.unload()
            } catch (e: Throwable) {
                logEx(e, "Failed to unload script directory $directory")
            }
        }
    }

    @CustomTask(taskId = "reload", priority = 5)
    fun reload() {
        invokeAll("onDisable")
        unload()
        scripts.clear()
        unloadHandlerDirectories()

        val root = FileUtils.saveDirectoryIfNotExists("scripts")

        FileLoader.load(
            target = root,
            processor = { file ->
                val engine = engines[file.extension.lowercase()] ?: return@load

                val relativePath = file.relativeTo(root).invariantSeparatorsPath
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
        )

        for ((directory, handler) in externalDirectories) {
            if (!directory.isDirectory) continue
            try {
                handler.load(directory)
            } catch (e: Throwable) {
                logEx(e, "Failed to load script directory $directory")
            }
        }

        invokeAll("onEnable")
        MessageType.LOAD_SCRIPTS.sendTo(Bukkit.getConsoleSender(), mapOf("count" to size.toString()))
    }

    private fun invokeAll(function: String) {
        for (script in scripts.values) {
            if (script.hasFunction(function)) {
                script.invokeSafely(function)
            }
        }
    }

}
