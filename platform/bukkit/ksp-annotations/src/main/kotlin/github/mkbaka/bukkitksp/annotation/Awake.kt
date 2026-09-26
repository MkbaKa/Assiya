package github.mkbaka.bukkitksp.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Awake(
    val lifeCycle: LifeCycle,
    val priority: Int = 0
)