package github.mkbaka.assiya.platform.bukkit.api.attribute.trigger

import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor

import java.util.function.Consumer

class TriggerHandler(
    val settings: TriggerSettings,
    val action: Consumer<BukkitProcessor>,
)
