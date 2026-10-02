package github.mkbaka.mythicksp.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MythicCondition(vararg val names: String)
