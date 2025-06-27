package felix.livinglink.eventSourcing.store

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import felix.livinglink.AppContext
import felix.livinglink.db.AppDatabase


actual class EventDefaultStore : EventStore by EventSqlDelightStore(
    AndroidSqliteDriver(
        schema = AppDatabase.Schema,
        context = AppContext.applicationContext,
        name = "event.db"
    )
)