package github.mkbaka.assiya.platform.bukkit.internal.config.reload

import github.mkbaka.assiya.platform.bukkit.internal.config.AttributeConfigManager
import github.mkbaka.assiya.platform.bukkit.internal.config.ConditionConfigManager
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.config.FormulaManager
import github.mkbaka.assiya.platform.bukkit.internal.config.ReaderConfigManager
import github.mkbaka.assiya.platform.bukkit.internal.config.ScriptManager
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicMobAttributes
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy.GroovyEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx

enum class ReloadModule {

    ALL {
        override fun reload(): Boolean = Config.reload()
    },

    SCRIPT {
        override fun reload(): Boolean {
            GroovyEngineAdapter.reload()
            ScriptManager.reload()
            return true
        }
    },

    READER {
        override fun reload(): Boolean = reloadWithIndex(name) { ReaderConfigManager.reload() }
    },

    FORMULA {
        override fun reload(): Boolean {
            FormulaManager.reload()
            return true
        }
    },

    CONDITION {
        override fun reload(): Boolean = reloadWithIndex(name) { ConditionConfigManager.reload() }
    },

    ATTRIBUTE {
        override fun reload(): Boolean = reloadWithIndex(name) { AttributeConfigManager.reload() }
    },

    MYTHICMOBS_CACHE {
        override fun reload(): Boolean {
            MythicMobAttributes.clear()
            return true
        }
    };

    abstract fun reload(): Boolean

    val id: String
        get() = name.lowercase()

    companion object {

        fun byId(id: String): ReloadModule? {
            val key = id.lowercase()
            return entries.firstOrNull { it.id == key }
        }

        private fun reloadWithIndex(module: String, action: () -> Unit): Boolean {
            AttributeRegistration.begin()
            var loaded = true
            try {
                action()
            } catch (e: Exception) {
                loaded = false
                logEx(e, "Failed to reload module $module")
            } finally {
                AttributeRegistration.complete()
            }

            Config.global.parseDefaultAttributes()
            AssiyaSessionRegistry.refresh()
            MythicMobAttributes.clear()
            return loaded
        }

    }

}
