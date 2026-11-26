package github.mkbaka.assiya.platform.bukkit.internal.hook.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.misc.Unregister
import org.bukkit.Bukkit
import org.bukkit.Particle

object ProtocolLibDamageIndicatorHook {

    fun register(): Unregister {
        // 1.13 起 WORLD_PARTICLES 带 ParticleParam, 可用 getNewParticles() 读取粒子类型;
        // 1.12 及更早只有 EnumParticle(数字 id), 只能用旧的 getParticles().
        val legacy = isLegacyParticle()
        val manager = ProtocolLibrary.getProtocolManager()
        val listener = object : PacketAdapter(
            AssiyaBukkitPlugin.INSTANCE,
            ListenerPriority.NORMAL,
            PacketType.Play.Server.WORLD_PARTICLES
        ) {
            override fun onPacketSending(event: PacketEvent) {
                if (legacy) cancelLegacy(event) else cancelModern(event)
            }
        }
        manager.addPacketListener(listener)
        return { manager.removePacketListener(listener) }
    }

    // 1.13+ / 1.20.5+ 的注册表 ParticleOptions 由 WrappedParticle 统一处理.
    private fun cancelModern(event: PacketEvent) {
        val particles = event.packet.newParticles
        if (particles.size() <= 0) {
            return
        }
        if (particles.read(0).particle == Particle.DAMAGE_INDICATOR) {
            event.isCancelled = true
        }
    }

    // 1.12 及更早的粒子枚举通过 getParticles() 转换为 EnumWrappers.Particle.
    private fun cancelLegacy(event: PacketEvent) {
        val particles = event.packet.particles
        if (particles.size() <= 0) {
            return
        }
        if (particles.read(0) == EnumWrappers.Particle.DAMAGE_INDICATOR) {
            event.isCancelled = true
        }
    }

    private fun isLegacyParticle(): Boolean {
        // Bukkit 版本形如 "1.12.2-R0.1-SNAPSHOT"; 1.13 之前用旧的数字 id 粒子包.
        val version = Bukkit.getBukkitVersion().substringBefore('-')
        val parts = version.split('.')
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return major <= 1 && minor < 13
    }

}
