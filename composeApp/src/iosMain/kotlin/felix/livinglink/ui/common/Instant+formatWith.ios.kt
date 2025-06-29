package felix.livinglink.ui.common

import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

actual fun Instant.formatWith(style: DateFormatStyle): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = when (style) {
            DateFormatStyle.DATE_ONLY -> "dd.MM.yyyy"
            DateFormatStyle.TIME_ONLY -> "HH:mm"
            DateFormatStyle.DATE_TIME -> "dd.MM.yyyy HH:mm"
        }
        timeZone = NSTimeZone.localTimeZone
    }

    val date = NSDate.dateWithTimeIntervalSince1970(this.epochSeconds.toDouble())
    return formatter.stringFromDate(date)
}