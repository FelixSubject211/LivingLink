package felix.livinglink.eventSourcing.store

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import felix.livinglink.db.AppDatabase

actual class AggregateDefaultStore : AggregateStore by AggregateSqlDelightStore(
    NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = "aggregate.db"
    )
)
