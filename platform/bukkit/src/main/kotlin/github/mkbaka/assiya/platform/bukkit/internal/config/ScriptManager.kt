package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.GroovyEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptScheduler
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
        return scripts[id.lowercase()]?.invokeSafely(function, *args)
    }

    operator fun get(id: String): Script? = scripts[id.lowercase()]

    private fun defaultResources(): List<String> {
        return listOf(
            "scripts/handler/Damage.groovy",
            "scripts/handler/Update.groovy",
            "scripts/handler/Timer.groovy",
            "scripts/handler/Kill.groovy",
            "scripts/attribute/Health.groovy",
            "scripts/attribute/health-regain.groovy",
            "scripts/attribute/Moving.groovy",
            "scripts/attribute/kill-example.groovy",
            "scripts/attribute/runtime-example.groovy",
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
            "scripts/attribute/fight/remote-immune.groovy",
            "scripts/attribute/fight/reflection.groovy",
            "scripts/attribute/fight/fire.groovy",
            "scripts/attribute/fight/Frozen.groovy",
            "scripts/condition/level-require.groovy",
            "scripts/condition/equipment-slot.groovy",
            "scripts/condition/permission.groovy",
            "scripts/feature/CombatStateListener.groovy"
        )
    }

    fun unload() {
        ScriptScheduler.unregister()
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
                if (scripts.putIfAbsent(id.lowercase(), engine.compile(id, file)) != null) {
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
