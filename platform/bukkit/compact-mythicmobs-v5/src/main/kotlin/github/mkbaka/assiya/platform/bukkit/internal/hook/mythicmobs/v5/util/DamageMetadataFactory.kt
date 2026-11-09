package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.util

import io.lumine.mythic.api.skills.SkillCaster
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.damage.DamageMetadata
import org.bukkit.event.entity.EntityDamageEvent
import java.lang.invoke.MethodHandles

 object DamageMetadataFactory {

    private val baseTypes = listOf(
        SkillCaster::class.java.name, "double", "java.util.Map", "java.util.Map",
        "java.lang.String", "double", "java.lang.Boolean", "java.lang.Boolean",
        "java.lang.Boolean", "java.lang.Boolean", EntityDamageEvent.DamageCause::class.java.name
    )

    private val itemType = "io.lumine.mythic.api.adapters.AbstractItemStack"

    private val layouts = listOf(
        listOf(baseTypes.first(), SkillMetadata::class.java.name, itemType) + baseTypes.drop(1),
        listOf(baseTypes.first(), itemType) + baseTypes.drop(1),
        baseTypes
    )

    private val selectedConstructor = layouts.firstNotNullOfOrNull { layout ->
        DamageMetadata::class.java.constructors.firstOrNull { candidate ->
            candidate.parameterTypes.map { it.name } == layout
        }
    } ?: error("Unsupported MythicMobs DamageMetadata constructor")

    private val lookup = MethodHandles.publicLookup()
    private val constructor = lookup.unreflectConstructor(selectedConstructor)

    private val withMetadata = selectedConstructor.parameterTypes.any { it == SkillMetadata::class.java }

    private val withItem = selectedConstructor.parameterTypes.any { it.name == itemType }

    private val casterItem = if (withItem) {
        lookup.unreflect(SkillMetadata::class.java.getMethod("getCasterItem"))
    } else null

    fun create(caster: SkillCaster, metadata: SkillMetadata): DamageMetadata {
        val arguments = arrayListOf<Any?>()
        arguments += caster
        if (withMetadata) arguments += metadata
        if (withItem) arguments += casterItem!!.invoke(metadata)
        arguments.addAll(
            listOf(
                0.0, hashMapOf<String, Double>(), hashMapOf<String, Double>(), null, 1.0,
                false, false, false, false, EntityDamageEvent.DamageCause.ENTITY_ATTACK
            )
        )
        return constructor.invokeWithArguments(arguments) as DamageMetadata
    }

}
