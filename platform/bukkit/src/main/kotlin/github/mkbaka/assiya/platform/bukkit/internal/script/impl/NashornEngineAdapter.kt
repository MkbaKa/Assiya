package github.mkbaka.assiya.platform.bukkit.internal.script.impl

import github.mkbaka.assiya.platform.bukkit.internal.script.Script
import github.mkbaka.assiya.platform.bukkit.internal.script.ScriptEngineAdapter
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import java.io.File
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptEngine
import github.mkbaka.assiya.libs.nashorn.api.scripting.NashornScriptEngineFactory as RelocatedFactory
import jdk.nashorn.api.scripting.NashornScriptEngineFactory as BuiltinFactory

internal object NashornEngineAdapter : ScriptEngineAdapter {

    private val ENGINE_ARGS = arrayOf("--language=es6", "-scripting", "--optimistic-types")

    private val newEngine: ((Array<String>, ClassLoader) -> ScriptEngine)? by lazy {
        try {
            val builtinPresent = try {
                Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
            if (builtinPresent) BuiltinNashorn::newEngine else RelocatedNashorn::newEngine
        } catch (e: Throwable) {
            logEx(e, "Nashorn script engine is unavailable, scripts will not be loaded")
            null
        }
    }

    override fun compile(id: String, file: File): Script {
        val factory = checkNotNull(newEngine) { "Nashorn script engine is unavailable" }
        val engine = factory(ENGINE_ARGS, javaClass.classLoader)
        file.reader(Charsets.UTF_8).use { reader ->
            (engine as Compilable).compile(reader).eval()
        }
        return NashornScript(id, engine)
    }

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
