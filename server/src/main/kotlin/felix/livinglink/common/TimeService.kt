package felix.livinglink.common

interface TimeService {
    fun currentTimeMillis(): Long
}

class TimeDefaultService : TimeService {
    override fun currentTimeMillis() = System.currentTimeMillis()
}