package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import felix.projekt.livinglink.composeApp.AppContext
import felix.projekt.livinglink.composeApp.eventDatabase.EventDatabase

actual class EventDatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        val context: Context = AppContext.context
        return AndroidSqliteDriver(EventDatabase.Schema, context, "events.db")
    }
}
