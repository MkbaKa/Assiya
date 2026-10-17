package github.mkbaka.assiya.platform.bukkit.internal.hook.neigeitems

import github.mkbaka.assiya.platform.bukkit.internal.registry.ItemReaderRegistry
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.Bukkit

object NeigeItemsHook {

    @Awake(lifeCycle = LifeCycle.ENABLE, priority = 100)
    fun register() {
        if (Bukkit.getPluginManager().isPluginEnabled("NeigeItems")) {
            ItemReaderRegistry.register("neigeitems", NeigeItemsNbtReader)
        }
    }

}
