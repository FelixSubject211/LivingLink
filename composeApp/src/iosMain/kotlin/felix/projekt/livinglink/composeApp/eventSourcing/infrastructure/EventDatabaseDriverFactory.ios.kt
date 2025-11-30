package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import felix.projekt.livinglink.composeApp.database.EventDatabase

actual class EventDatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(EventDatabase.Schema, "events.db")
    }
}
