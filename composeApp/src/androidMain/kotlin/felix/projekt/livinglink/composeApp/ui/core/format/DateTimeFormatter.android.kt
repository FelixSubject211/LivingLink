package felix.projekt.livinglink.composeApp.ui.core.format

import java.text.DateFormat
import java.util.Date
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
actual fun formatDateTime(instant: Instant): String {
    val formatter = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.MEDIUM
    )
    return formatter.format(Date(instant.toEpochMilliseconds()))
}