package felix.livinglink.ui.common

import kotlinx.datetime.Instant

enum class DateFormatStyle {
    DATE_ONLY,
    TIME_ONLY,
    DATE_TIME
}

expect fun Instant.formatWith(style: DateFormatStyle = DateFormatStyle.DATE_TIME): String