package github.mkbaka.bukkitksp.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomTask(
    val taskId: String,
    val priority: Int = 0
)
