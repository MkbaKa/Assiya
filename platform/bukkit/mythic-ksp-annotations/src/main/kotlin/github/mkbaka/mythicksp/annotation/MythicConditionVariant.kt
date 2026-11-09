package github.mkbaka.mythicksp.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MythicConditionVariant(val base: KClass<*>, val requiresClass: String)
