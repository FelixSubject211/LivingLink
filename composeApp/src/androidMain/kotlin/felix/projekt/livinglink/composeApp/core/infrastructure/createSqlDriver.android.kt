package felix.projekt.livinglink.composeApp.core.infrastructure

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import felix.projekt.livinglink.composeApp.AppContext
import felix.projekt.livinglink.composeApp.core.Database

actual fun createSqlDriver(): SqlDriver {
    val context: Context = AppContext.context
    return AndroidSqliteDriver(Database.Schema, context, "database.db")
}