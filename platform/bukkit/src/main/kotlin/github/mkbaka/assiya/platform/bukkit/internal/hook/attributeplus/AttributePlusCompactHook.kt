package github.mkbaka.assiya.platform.bukkit.internal.hook.attributeplus

import github.mkbaka.assiya.platform.bukkit.api.event.ModuleReloadEvent
import github.mkbaka.assiya.platform.bukkit.internal.config.reload.ReloadModule
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.on
import github.mkbaka.assiya.platform.bukkit.internal.util.hasClass
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle

object AttributePlusCompactHook {

    private var hooked = false

    @Awake(lifeCycle = LifeCycle.ENABLE, priority = 100)
    fun register() {
        hooked = hasClass("org.serverct.ersha.AssiyaAttributePlusCompact")

        if (hooked) {
            on<ModuleReloadEvent.Pre> {
                if (module == ReloadModule.SCRIPT) {
                    requireAttributeRefresh()
                }
            }
        }
    }

}
