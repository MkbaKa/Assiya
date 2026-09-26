package github.mkbaka.bukkitksp.processor

internal fun findCollidingSimpleNames(objectNames: List<String>): Set<String> {
    return objectNames
        .groupBy { it.substringAfterLast('.') }
        .filter { (_, fqns) -> fqns.distinct().size > 1 }
        .keys
}

internal fun resolveCallName(objectName: String, collidingSimples: Set<String>): String {
    val simple = objectName.substringAfterLast('.')
    return if (simple in collidingSimples) objectName else simple
}
