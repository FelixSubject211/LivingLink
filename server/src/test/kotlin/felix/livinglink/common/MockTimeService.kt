package felix.livinglink.common

import kotlinx.datetime.Clock

object MockTimeService : TimeService {
    override fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}