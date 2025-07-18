package felix.livinglink.eventSourcing.store

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import felix.livinglink.AppContext
import felix.livinglink.db.AppDatabase

actual class AggregateDefaultStore : AggregateStore by AggregateSqlDelightStore(
    AndroidSqliteDriver(
        schema = AppDatabase.Schema,
        context = AppContext.applicationContext,
        name = "aggregate.db"
    )
)
