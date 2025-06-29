package felix.livinglink.ui.common

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
actual fun Instant.formatWith(style: DateFormatStyle): String {
    val pattern = when (style) {
        DateFormatStyle.DATE_ONLY -> "dd.MM.yyyy"
        DateFormatStyle.TIME_ONLY -> "HH:mm"
        DateFormatStyle.DATE_TIME -> "dd.MM.yyyy HH:mm"
    }

    val formatter = DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneId.systemDefault())

    return formatter.format(this.toJavaInstant())
}