package github.mkbaka.assiya.platform.bukkit.internal.script.impl

import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import java.io.File
import javax.script.*
import github.mkbaka.assiya.libs.nashorn.api.scripting.NashornScriptEngineFactory as RelocatedFactory
import jdk.nashorn.api.scripting.NashornScriptEngineFactory as BuiltinFactory

object NashornEngineAdapter : ScriptEngineAdapter {

    private val ENGINE_ARGS = arrayOf("--language=es6", "-scripting", "--optimistic-types")

    private val newEngine: ((Array<String>, ClassLoader) -> ScriptEngine) by lazy {
        val builtinPresent = try {
            Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        if (builtinPresent) BuiltinNashorn::newEngine else RelocatedNashorn::newEngine
    }

    val globalEngine = newEngine(arrayOf("--global-per-engine", *ENGINE_ARGS), this::class.java.classLoader)

    fun eval(expression: String, args: Map<String, Any>): Any? {
        return try {
            globalEngine.eval(expression, SimpleBindings(args))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun eval(script: CompiledScript, args: Map<String, Any>): Any? {
        return try {
            script.eval(SimpleBindings(args))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun compile(id: String, file: File): Script {
        val engine = createEngine(javaClass.classLoader)
        file.reader(Charsets.UTF_8).use { reader ->
            (engine as Compilable).compile(reader).eval()
        }
        return NashornScript(id, engine)
    }

    fun createEngine(loader: ClassLoader): ScriptEngine = newEngine(ENGINE_ARGS, loader)

    private class NashornScript(
        override val id: String,
        private val engine: ScriptEngine,
    ) : Script {

        override fun hasFunction(function: String): Boolean {
            return engine.get(function) != null
        }

        override fun invoke(function: String, vararg args: Any?): Any? {
            return (engine as Invocable).invokeFunction(function, *args)
        }
    }

    private object BuiltinNashorn {

        private val factory = BuiltinFactory()

        fun newEngine(args: Array<String>, loader: ClassLoader): ScriptEngine {
            return factory.getScriptEngine(args, loader)
        }
    }

    private object RelocatedNashorn {

        private val factory = RelocatedFactory()

        fun newEngine(args: Array<String>, loader: ClassLoader): ScriptEngine {
            return factory.getScriptEngine(args, loader)
        }
    }

}
