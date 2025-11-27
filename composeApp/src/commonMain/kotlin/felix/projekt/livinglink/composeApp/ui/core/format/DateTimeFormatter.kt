package felix.projekt.livinglink.composeApp.ui.core.format

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
expect fun formatDateTime(instant: Instant): String