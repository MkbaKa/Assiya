package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.reader.Reader


object ReaderRegistry : NamedRegistry<Reader>("reader") {

    override fun register(id: String, value: Reader) {
        super.register(id, value)
        try {
            AttributeRegistration.refresh()
        } catch (cause: Throwable) {
            super.unregister(id, value)
            throw cause
        }
    }

    override fun unregister(id: String, value: Reader): Boolean {
        if (!super.unregister(id, value)) return false
        AttributeRegistration.refresh()
        return true
    }

    fun register(reader: Reader) {
        register(reader.id, reader)
    }

    fun unregister(reader: Reader): Boolean {
        return unregister(reader.id, reader)
    }

}
