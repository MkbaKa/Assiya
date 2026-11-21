package github.mkbaka.assiya.platform.bukkit.internal.script

import java.io.File

interface ScriptEngineAdapter {

    fun compile(id: String, file: File): Script

    fun eval(content: String, args: Map<String, Any?>): Any?

}
