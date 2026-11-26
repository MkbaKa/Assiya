package github.mkbaka.assiya.platform.bukkit.internal.hook.packet

import github.mkbaka.assiya.platform.bukkit.api.misc.Unregister
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.message.MessageType
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.CustomTask
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.Bukkit

object DamageIndicatorParticleHook {

    private var unregister: Unregister? = null

    @CustomTask(taskId = "reload", priority = 3)
    fun reload() {
        close()
        if (!Config.global.BLOCK_DAMAGE_INDICATOR) return

        MessageType.DAMAGE_INDICATOR_ENABLED.sendTo(Bukkit.getConsoleSender())

        when {
            Bukkit.getPluginManager().isPluginEnabled("packetevents") -> {
                unregister = PacketEventsDamageIndicatorHook.register()
                MessageType.DAMAGE_INDICATOR_PACKET_EVENTS.sendTo(Bukkit.getConsoleSender())
            }

            Bukkit.getPluginManager().isPluginEnabled("ProtocolLib") -> {
                unregister = ProtocolLibDamageIndicatorHook.register()
                MessageType.DAMAGE_INDICATOR_PROTOCOL_LIB.sendTo(Bukkit.getConsoleSender())
            }
        }
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    fun close() {
        unregister?.unregister()
        unregister = null
    }

}
