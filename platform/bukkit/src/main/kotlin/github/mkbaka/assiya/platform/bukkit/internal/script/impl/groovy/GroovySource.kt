package github.mkbaka.assiya.platform.bukkit.internal.script.impl.groovy

import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

internal data class GroovySource(
    val name: String,
    val text: String,
    val lastModified: FileTime,
    val size: Long,
) {

    companion object {

        fun read(file: File, attributes: BasicFileAttributes): GroovySource {
            val bytes = file.readBytes()

            return GroovySource(
                file.absolutePath,
                bytes.toString(Charsets.UTF_8),
                attributes.lastModifiedTime(),
                attributes.size(),
            )
        }

    }

}
