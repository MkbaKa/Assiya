package github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy

internal data class GroovyArtifact(
    val source: GroovySource,
    val mainClassName: String,
    val classes: Map<String, ByteArray>
)
