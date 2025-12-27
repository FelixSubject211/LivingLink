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
            override fun onConfigure(db: SupportSQLiteDatabase) {
                super.onConfigure(db)
                setPragma(db, "JOURNAL_MODE = WAL")
                setPragma(db, "SYNCHRONOUS = 2")
            }

            private fun setPragma( db: SupportSQLiteDatabase, pragma: String) {
                val cursor = db.query("PRAGMA $pragma")
                cursor.moveToFirst()
                cursor.close()
            }
        }
    )
}