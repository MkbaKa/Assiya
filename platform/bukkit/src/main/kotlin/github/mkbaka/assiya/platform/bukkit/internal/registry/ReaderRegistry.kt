package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.reader.Reader


object ReaderRegistry : Registry<Reader>("reader") {

    fun register(reader: Reader) {
        register(reader.id, reader)
    }

    fun unregister(reader: Reader): Boolean {
        return unregister(reader.id, reader)
    }

}
