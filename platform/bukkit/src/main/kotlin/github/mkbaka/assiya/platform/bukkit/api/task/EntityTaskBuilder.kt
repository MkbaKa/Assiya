package github.mkbaka.assiya.platform.bukkit.api.task

class EntityTaskBuilder {

    var key: String? = null

    var delay: Long = 0

    var period: Long = 0

    var count: Int? = 1

    var async: Boolean = false

    var params: Map<String, Any> = emptyMap()

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