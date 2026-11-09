package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs

import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.Bukkit

object MythicMobsHook {

    @Awake(lifeCycle = LifeCycle.ENABLE, priority = 100)
    fun register() {
        val plugin = Bukkit.getPluginManager().getPlugin("MythicMobs") ?: return

        val implementation = when (plugin.javaClass.name) {
            "io.lumine.xikage.mythicmobs.MythicMobs" ->
                "github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.MythicMobs4Hook"

            "io.lumine.mythic.bukkit.MythicBukkit" ->
                "github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.MythicMobs5Hook"

            else -> error("Unsupported MythicMobs entry: ${plugin.javaClass.name}")
        }

        val type = Class.forName(implementation, true, this::class.java.classLoader)
        val compatibility = type.getDeclaredConstructor().newInstance() as MythicMobsCompatibility
        compatibility.register()
    }

}
