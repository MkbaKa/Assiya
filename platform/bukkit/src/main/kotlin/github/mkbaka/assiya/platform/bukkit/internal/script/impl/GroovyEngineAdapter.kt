package github.mkbaka.assiya.platform.bukkit.internal.script.impl

import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.bukkitksp.annotation.CustomTask
import groovy.lang.Binding
import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyObject
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.InvokerHelper
import java.io.File
import groovy.lang.Script as GroovyScriptBase

internal object GroovyEngineAdapter : ScriptEngineAdapter {

    @Volatile
    private var loader: GroovyClassLoader? = null

    @CustomTask(taskId = "reload", priority = 4)
    fun reload() {
        val config = CompilerConfiguration().apply {
            sourceEncoding = "UTF-8"
            targetBytecode = CompilerConfiguration.JDK8
        }
        // 重载需要使用新的 ClassLoader
        // 不然旧脚本类不会被释放
        val replacement = GroovyClassLoader(javaClass.classLoader, config)
        val previous = loader
        loader = replacement
        previous?.close()
    }

    override fun compile(id: String, file: File): Script {
        val activeLoader = checkNotNull(loader) { "Groovy engine has not been initialized" }
        val mainClass = activeLoader.parseClass(file)
        // 显式声明 class
        val instance: GroovyObject = if (GroovyScriptBase::class.java.isAssignableFrom(mainClass)) {
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
