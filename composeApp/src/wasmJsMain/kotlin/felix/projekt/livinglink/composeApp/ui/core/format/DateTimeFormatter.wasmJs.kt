package felix.projekt.livinglink.composeApp.ui.core.format

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
actual fun formatDateTime(instant: Instant): String {
    return jsFormatLocal(instant.toEpochMilliseconds().toDouble())
}

@JsFun("ms => new Intl.DateTimeFormat([], { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(ms))")
private external fun jsFormatLocal(ms: Double): String
