package github.mkbaka.assiya.platform.bukkit.internal.hook.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle
import github.mkbaka.assiya.platform.bukkit.api.misc.Unregister

object PacketEventsDamageIndicatorHook {

    fun register(): Unregister {
        val manager = PacketEvents.getAPI().eventManager
        val listener = manager.registerListener(object : PacketListener {
            override fun onPacketSend(event: PacketSendEvent) {
                if (event.packetType != PacketType.Play.Server.PARTICLE) {
                    return
                }
                val packet = WrapperPlayServerParticle(event)
                // 1.20.5 起的注册表 ParticleOptions 与旧版布局由 wrapper 统一处理.
                if (packet.particle.type == ParticleTypes.DAMAGE_INDICATOR) {
                    event.isCancelled = true
                }
            }
        }, PacketListenerPriority.NORMAL)
        return { manager.unregisterListener(listener) }
    }

}
