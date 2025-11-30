package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import app.cash.sqldelight.db.SqlDriver
import felix.projekt.livinglink.composeApp.database.EventDatabase

expect class EventDatabaseDriverFactory() {
    fun createDriver(): SqlDriver
}

class EventDatabaseFactory(
    private val driverFactory: EventDatabaseDriverFactory
) {
    fun createDatabase(): EventDatabase {
        return EventDatabase(driverFactory.createDriver())
    }
}
