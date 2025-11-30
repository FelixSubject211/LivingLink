package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import app.cash.sqldelight.db.SqlDriver

actual class EventDatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        throw NotImplementedError()
    }
}
