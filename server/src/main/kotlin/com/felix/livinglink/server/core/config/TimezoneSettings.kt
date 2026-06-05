package com.felix.livinglink.server.core.config

import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Single

@Single
class TimezoneSettings {
    val timezoneId: String = Env.required("LIVINGLINK_TIMEZONE")

    val timeZone: TimeZone by lazy {
        TimeZone.of(timezoneId)
    }
}
