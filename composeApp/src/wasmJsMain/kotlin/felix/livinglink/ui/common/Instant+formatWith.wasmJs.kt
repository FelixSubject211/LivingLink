package felix.livinglink.ui.common

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual fun Instant.formatWith(style: DateFormatStyle): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())

    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val year = dateTime.year.toString()
    val hours = dateTime.hour.toString().padStart(2, '0')
    val minutes = dateTime.minute.toString().padStart(2, '0')

    return when (style) {
        DateFormatStyle.DATE_ONLY -> "$day.$month.$year"
        DateFormatStyle.TIME_ONLY -> "$hours:$minutes"
        DateFormatStyle.DATE_TIME -> "$day.$month.$year $hours:$minutes"
    }
}