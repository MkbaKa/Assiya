package github.mkbaka.mythicksp.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MythicMechanic(vararg val names: String)
