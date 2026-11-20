package github.mkbaka.assiya.platform.bukkit.internal.script

import java.io.File

interface ScriptDirectoryHandler {

    fun unload()

    fun load(directory: File)
}
