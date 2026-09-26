package github.mkbaka.bukkitksp.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomField(
    val fieldType: String
)