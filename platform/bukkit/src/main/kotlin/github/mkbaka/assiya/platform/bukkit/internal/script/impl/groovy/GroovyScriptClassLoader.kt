package github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy

import groovy.lang.GroovyClassLoader
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases

internal class GroovyScriptClassLoader(parent: ClassLoader) : GroovyClassLoader(parent) {

    private var pendingClasses: Map<String, ByteArray> = emptyMap()

    fun compile(source: GroovySource): GroovyArtifact {
        val config = CompilerConfiguration().apply {
            sourceEncoding = "UTF-8"
            targetBytecode = CompilerConfiguration.JDK8
        }

        val unit = CompilationUnit(config, null, this)
        val sourceUnit = unit.addSource(source.name, source.text)
        unit.compile(Phases.CLASS_GENERATION)

        // 可能会有 内部类/lambda 之类的情况  一起塞进缓存
        val classes = linkedMapOf<String, ByteArray>()
        for (generated in unit.classes) {
            classes[generated.name] = generated.bytes
        }

        return GroovyArtifact(source, sourceUnit.ast.mainClassName, classes)
    }

    fun load(artifact: GroovyArtifact): Class<*> {
        pendingClasses = artifact.classes
        try {
            for (name in artifact.classes.keys) {
                loadClass(name)
            }
            return loadClass(artifact.mainClassName)
        } finally {
            pendingClasses = emptyMap()
        }
    }

    override fun findClass(name: String): Class<*> {
        val bytes = pendingClasses[name] ?: return super.findClass(name)
        val type = defineClass(name, bytes)
        setClassCacheEntry(type)
        return type
    }

}
