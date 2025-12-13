package felix.projekt.livinglink.composeApp.core.infrastructure

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import felix.projekt.livinglink.composeApp.core.Database

actual fun createSqlDriver(): SqlDriver {
    return NativeSqliteDriver(Database.Schema, "database.db")
}