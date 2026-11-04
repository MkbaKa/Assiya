package github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy

import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.bukkitksp.annotation.CustomTask
import groovy.lang.Binding
import groovy.lang.GroovyObject
import org.codehaus.groovy.runtime.InvokerHelper
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import groovy.lang.Script as GroovyScriptBase

object GroovyEngineAdapter : ScriptEngineAdapter {

    private val cache = mutableMapOf<String, GroovyArtifact>()

    @Volatile
    private var loader = GroovyScriptClassLoader(javaClass.classLoader)

    @CustomTask(taskId = "reload", priority = 4)
    fun reload() {
        // 清理被删掉的文件
        cache.keys.removeAll { !File(it).isFile }

        // 重载需要使用新的 ClassLoader
        // 不然旧脚本类不会被释放
        val previous = loader
        loader = GroovyScriptClassLoader(javaClass.classLoader)
        previous.close()
    }

    override fun compile(id: String, file: File): Script {
        // 先前解析过的结果
        val cached = cache.remove(file.absolutePath)

        val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        // 如果文件没有改动就可以复用之前解析过的字节码
        // 因为 groovy 解析字节码的耗时肥肠滴恐怖
        val artifact = if (cached != null &&
            cached.source.lastModified == attributes.lastModifiedTime() &&
            cached.source.size == attributes.size()
        ) {
            cached
        } else {
            val source = GroovySource.read(file, attributes)
            loader.compile(source)
        }

        cache[file.absolutePath] = artifact

        val mainClass = loader.load(artifact)
        val instance = if (GroovyScriptBase::class.java.isAssignableFrom(mainClass)) {
            InvokerHelper.createScript(mainClass, Binding()).also { it.run() }
        } else {
            // Groovy 编译出的类必然实现 GroovyObject
            mainClass.getDeclaredConstructor().newInstance() as GroovyObject
        }

        return GroovyScript(id, instance)
    }

    private class GroovyScript(
        override val id: String,
        private val instance: GroovyObject,
    ) : Script {

        private val methods = instance.javaClass.declaredMethods.asSequence().map { it.name }.toHashSet()

        override fun hasFunction(function: String): Boolean {
            return function in methods
        }

        override fun invoke(function: String, vararg args: Any?): Any? {
            return instance.invokeMethod(function, args)
        }

    }

}
