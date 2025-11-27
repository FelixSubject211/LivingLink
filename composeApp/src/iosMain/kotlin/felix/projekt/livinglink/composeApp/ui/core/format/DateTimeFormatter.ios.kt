package felix.projekt.livinglink.composeApp.ui.core.format

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
actual fun formatDateTime(instant: Instant): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "dd.MM.yyyy HH:mm"
    }
    val date = NSDate.dateWithTimeIntervalSince1970(instant.toEpochMilliseconds().toDouble() / 1_000.0)
    return formatter.stringFromDate(date)
}
