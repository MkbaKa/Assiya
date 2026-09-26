package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.GroovyEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import github.mkbaka.assiya.platform.bukkit.internal.util.FileUtils.getSubFiles
import github.mkbaka.assiya.platform.bukkit.internal.util.warning
import github.mkbaka.bukkitksp.annotation.CustomTask
import java.io.File
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
        return scripts[id]?.invokeSafely(function, *args)
    }

    operator fun get(id: String): Script? = scripts[id]

    private fun defaultResources(): List<String> {
        return listOf(
            "scripts/attribute/health.groovy",
            "scripts/attribute/fight/attack.groovy",
            "scripts/attribute/fight/pvp-attack.groovy",
            "scripts/attribute/fight/pve-attack.groovy",
            "scripts/attribute/fight/defense.groovy",
            "scripts/attribute/fight/pvp-defense.groovy",
            "scripts/attribute/fight/pve-defense.groovy",
            "scripts/attribute/fight/crit.groovy",
            "scripts/attribute/fight/lightning.groovy",
            "scripts/attribute/fight/vampire.groovy",
            "scripts/attribute/fight/armor.groovy",
            "scripts/attribute/fight/dodge.groovy",
            "scripts/condition/level-require.groovy"
//            "scripts/condition/player-level.js",
//            "scripts/damage/example.js",
//            "scripts/damage/attack.groovy",
//            "scripts/damage/real-attack.groovy",
//            "scripts/damage/break-shield.groovy",
//            "scripts/damage/fire.groovy",
//            "scripts/damage/frozen.groovy",
//            "scripts/damage/lightning.groovy",
//            "scripts/damage/crit.groovy",
//            "scripts/damage/vampire.groovy",
//            "scripts/damage/dodge.groovy",
//            "scripts/damage/shield-block.groovy",
//            "scripts/damage/defense.groovy",
//            "scripts/damage/armor.groovy",
//            "scripts/damage/reflection.groovy",
//            "scripts/runtime/restore.groovy",
//            "scripts/update/max-health.groovy",
//            "scripts/update/moving.groovy",
        )
    }

    fun unload() {
        ScriptListener.unregister()
    }

    @CustomTask(taskId = "reload", priority = 5)
    fun reload() {
        invokeAll("onDisable")
        unload()
        scripts.clear()

        val directory = File(AssiyaBukkitPlugin.INSTANCE.dataFolder, "scripts")
        if (!directory.exists()) {
            for (resource in defaultResources()) {
                AssiyaBukkitPlugin.INSTANCE.saveResource(resource, false)
            }
        }
        for (file in directory.getSubFiles()) {
            val engine = engines[file.extension.lowercase()] ?: continue
            val relativePath = file.invariantSeparatorsPath.substringAfterLast("scripts/")
            val id = relativePath.substringBeforeLast('.')
            try {
                if (scripts.putIfAbsent(id, engine.compile(id, file)) != null) {
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
