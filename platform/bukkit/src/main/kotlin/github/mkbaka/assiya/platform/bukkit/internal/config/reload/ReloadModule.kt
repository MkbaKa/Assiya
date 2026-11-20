package github.mkbaka.assiya.platform.bukkit.internal.config.reload

import github.mkbaka.assiya.platform.bukkit.api.event.ModuleReloadEvent
import github.mkbaka.assiya.platform.bukkit.internal.CustomTasks
import github.mkbaka.assiya.platform.bukkit.internal.config.*
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicMobAttributes
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy.GroovyEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call

enum class ReloadModule(private val requiresAttributeRefresh: Boolean = false) {

    ALL(requiresAttributeRefresh = true) {
        override fun reload0() {
            CustomTasks.run("reload")
        }
    },

    SCRIPT {
        override fun reload0() {
            GroovyEngineAdapter.reload()
            ScriptManager.reload()
        }
    },

    READER(requiresAttributeRefresh = true) {
        override fun reload0() {
            ReaderConfigManager.reload()
        }
    },

    FORMULA {
        override fun reload0() {
            FormulaManager.reload()
        }
    },

    CONDITION(requiresAttributeRefresh = true) {
        override fun reload0() {
            ConditionConfigManager.reload()
        }
    },

    ATTRIBUTE(requiresAttributeRefresh = true) {
        override fun reload0() {
            AttributeConfigManager.reload()
        }
    },

    MYTHICMOBS_CACHE {
        override fun reload0() {
            MythicMobAttributes.clear()
        }
    };

    protected abstract fun reload0()

    fun reload() {
        val pre = ModuleReloadEvent.Pre(this, requiresAttributeRefresh)
        pre.call()
        if (this == ALL) Config.load()
        if (pre.requiresAttributeRefresh) {
            reloadWithIndex(name) { reload0() }
        } else {
            reload0()
        }
        ModuleReloadEvent.Post(this).call()
    }

    val id: String
        get() = name.lowercase()

    companion object {

        fun byId(id: String): ReloadModule? {
            val key = id.lowercase()
            return entries.firstOrNull { it.id == key }
        }

        private fun reloadWithIndex(module: String, action: () -> Unit) {
            AttributeRegistration.begin()

            try {
                action()
            } finally {
                AttributeRegistration.complete()
                Config.global.parseDefaultAttributes()
                AssiyaSessionRegistry.refresh()

                if (module != ALL.name) MythicMobAttributes.clear()
            }
        }

    }

}
