package github.mkbaka.assiya.platform.bukkit.api.task

class EntityTaskBuilder {

    var key: String? = null
        private set
    var delay: Long = 0
        private set
    var period: Long = 0
        private set
    var count: Int? = 1
        private set
    var async: Boolean = false
        private set
    var params: Map<String, Any> = emptyMap()
        private set

    fun key(key: String?): EntityTaskBuilder {
        this.key = key
        return this
    }

    fun delay(delay: Long): EntityTaskBuilder {
        this.delay = delay
        return this
    }

    fun period(period: Long): EntityTaskBuilder {
        this.period = period
        return this
    }

    fun count(count: Int?): EntityTaskBuilder {
        this.count = count
        return this
    }

    fun async(async: Boolean): EntityTaskBuilder {
        this.async = async
        return this
    }

    fun params(params: Map<String, Any>): EntityTaskBuilder {
        this.params = params
        return this
    }

}