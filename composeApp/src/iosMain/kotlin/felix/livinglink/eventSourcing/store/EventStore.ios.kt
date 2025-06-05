package felix.livinglink.eventSourcing.store

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import felix.livinglink.db.AppDatabase


actual class EventDefaultStore : EventStore by EventSqlDelightStore(
    NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = "event.db"
    )
)