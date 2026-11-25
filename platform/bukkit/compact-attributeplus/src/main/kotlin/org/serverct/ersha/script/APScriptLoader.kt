package org.serverct.ersha.script

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.HandlerComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.SimpleTriggerSettings
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerHandler
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.*
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.registry.TriggerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptDirectoryHandler
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.serverct.ersha.AttributePlus
import org.serverct.ersha.attribute.ApAttributeOptionsComponent
import org.serverct.ersha.attribute.ApWrappedAttribute
import org.serverct.ersha.attribute.AttributeHandle
import java.io.File
import java.util.function.Consumer
import java.util.logging.Level
import javax.script.Invocable

object APScriptLoader : ScriptDirectoryHandler {
    private val attributes = arrayListOf<Attribute>()
    private val priorities = hashSetOf<Int>()

    override fun unload() {
        AttributeRegistration.batch {
            for (attribute in attributes.toList()) {
                attribute.unregister()
                attributes.remove(attribute)
            }
        }
        priorities.clear()
    }

    override fun load(directory: File) {
        val imports = AttributePlus.instance.loadScriptTools()
        AttributeRegistration.batch {
            for (file in directory.walkTopDown().filter { it.isFile }) {
                try {
                    loadScript(file, imports)
                } catch (e: Exception) {
                    AttributePlus.instance.logger.log(Level.SEVERE, "Failed to load AP script $file", e)
                }
            }
        }
    }

    fun loadScript(file: File, imports: Map<String, String>) {
        val engine = NashornEngineAdapter.createEngine(javaClass.classLoader)

        for ((name, type) in imports) engine.eval("var $name = Java.type('$type');")
        file.reader(Charsets.UTF_8).use { engine.eval(it) }

        val name = requireNotNull(engine.get("attributeName")) { "Missing attributeName" }.toString()
        val placeholder = requireNotNull(engine.get("placeholder")) { "Missing placeholder" }.toString()
        val readerId = engine.get("reader")?.toString() ?: "default"
        val type = requireNotNull(engine.get("attributeType")).toString()
        require(type != "CUSTOM") { "AP CUSTOM attributes are not supported" }
        val priority = (engine.get("priority") as Number).toInt()
        val power = (engine.get("combatPower") as Number).toDouble()
        val attribute = ScriptAttribute(placeholder, name, power, readerId)

        attribute.options["placeholder"] = placeholder
        attribute.options["priority"] = priority

        val wrapped = ApWrappedAttribute(attribute, type)
        val runtime = engine as Invocable
        runtime.invokeFunction("onLoad", wrapped)

        val triggerId = when (type) {
            "ATTACK" -> "attack"
            "DEFENSE" -> "defense"
            "UPDATE" -> "update"
            "KILLER" -> "kill"
            "RUNTIME" -> "timer"
            "OTHER" -> null
            else -> error("Unsupported AP attribute type: $type")
        }

        if (triggerId != null && wrapped.priority != -1) {
            var order = wrapped.priority
            while (!priorities.add(order)) order++

            val trigger = TriggerRegistry[triggerId]?.trigger ?: Trigger.Base(triggerId, "run")
                .also { TriggerRegistry.register(it) }
            val settings = SimpleTriggerSettings(
                priority = order,
                alwaysRun = true,
                ignoreCancelled = attribute.options["ignore-cancelled"] == true,
                periodTicks = 100L,
            )

            val callback = Consumer<BukkitProcessor> { processor ->
                val actor = if (type == "DEFENSE") processor[TargetComponent]?.entity else
                    processor[ProjectileComponent]?.shooter?.entity ?: processor[TriggerComponent]?.entity
                if (actor is LivingEntity) {
                    try {
                        val handle = AttributeHandle(processor)
                        if (processor[CancellableAttributeComponent]?.isCancelled("${actor.uniqueId}:${attribute.id}") == true) {
                            return@Consumer
                        }
                        if (attribute.options["skip-filter"] != true) {
                            val value = wrapped.container(actor, processor)?.getValue<Number>(attribute.id, "total")
                                ?.toDouble() ?: 0.0
                            if (value + handle.getTempAttributeValue(actor, attribute.id) <= 0.0) return@Consumer
                        }
                        if (type == "ATTACK" || type == "DEFENSE") installTargetDamage(processor)
                        val result = when (type) {
                            "ATTACK" -> runtime.invokeFunction(
                                "runAttack",
                                wrapped,
                                actor,
                                processor[TargetComponent]?.entity,
                                handle
                            )

                            "DEFENSE" -> runtime.invokeFunction(
                                "runDefense",
                                wrapped,
                                actor,
                                processor[ProjectileComponent]?.shooter?.entity ?: processor[TriggerComponent]?.entity,
                                handle
                            )

                            "KILLER" -> runtime.invokeFunction(
                                "runKiller",
                                wrapped,
                                actor,
                                processor[TargetComponent]?.entity,
                                handle
                            )

                            else -> runtime.invokeFunction("run", wrapped, actor, handle)
                        }
                        if (result == true) wrapped.onTriggered(processor)
                    } catch (e: Exception) {
                        AttributePlus.instance.logger.log(Level.SEVERE, "Failed to execute AP script $file", e)
                    }
                }
            }
            val action = TriggerHandler(settings) { processor ->
                val sync = (type == "RUNTIME" || type == "UPDATE") &&
                    (attribute.options["asynchronous"] == false || attribute.options["sync-update"] == true)
                if (sync && !Bukkit.isPrimaryThread()) {
                    Bukkit.getScheduler().callSyncMethod(AssiyaBukkitPlugin.INSTANCE) {
                        callback.accept(processor)
                    }.get()
                } else {
                    callback.accept(processor)
                }
            }
            attribute.mutableComponents[HandlerComponent] = HandlerComponent(mapOf(trigger to action))
        }
        if (wrapped.priority != -1 || type == "OTHER") register(attribute)
    }

    private fun installTargetDamage(processor: BukkitProcessor) {
        val data = processor[CustomDataComponent] ?: return
        val handle = AttributeHandle(processor)
        handle.tempValue.putIfAbsent("attack", processor[DamageMultipleComponent]?.getDamage("vanilla") ?: 0.0)
        if (data.putIfAbsent("attributeplus:target-damage", 0.0) != null) return
        processor[CallbackComponent]?.callback(false, "attributeplus:target-damage", Runnable {
            val value = (data["attributeplus:target-damage"] as? Number)?.toDouble() ?: 0.0
            handle.updateTempValue(true, "attackerDamage", processor[DamageMultipleComponent]?.totalDamage ?: 0.0)
            handle.updateTempValue(true, "entityDamage", value)
            val target = processor[TargetComponent]?.entity ?: return@Runnable
            val attacker = processor[ProjectileComponent]?.shooter?.entity ?: processor[TriggerComponent]?.entity
            ?: return@Runnable
            if (value > 0.0 && attacker is LivingEntity) AssiyaAPI.attackTo(target, attacker, value)
        })
    }

    fun registerOther(name: String, power: Double, placeholder: String) {
        val attribute = ScriptAttribute(placeholder, name, power)
        attribute.options["placeholder"] = placeholder
        register(attribute)
    }

    private fun register(attribute: Attribute) {
        attribute.register()
        attributes += attribute
    }

    private class ScriptAttribute(
        override val id: String,
        name: String,
        power: Double,
        readerId: String = "default"
    ) : Attribute {

        val options = ApAttributeOptionsComponent(hashMapOf("combat-power" to power))

        val mutableComponents = hashMapOf(
            ReaderComponent to ReaderComponent(readerId, listOf(name)),
            ApAttributeOptionsComponent to options
        )

        override val components: Map<AttributeComponent.Key<*>, AttributeComponent> get() = mutableComponents

    }

}
