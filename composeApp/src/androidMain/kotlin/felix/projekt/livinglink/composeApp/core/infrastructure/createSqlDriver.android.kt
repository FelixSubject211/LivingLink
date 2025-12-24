package felix.projekt.livinglink.composeApp.core.infrastructure

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import felix.projekt.livinglink.composeApp.AppContext
import felix.projekt.livinglink.composeApp.core.Database

actual fun createSqlDriver(): SqlDriver {
    val context: Context = AppContext.context
    return AndroidSqliteDriver(
        schema = Database.Schema,
        context = context,
        name = "database.db",
        callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.query("PRAGMA journal_mode=WAL;").use { cursor -> }
                db.execSQL("PRAGMA synchronous=NORMAL;")
            }
        }
    )
}