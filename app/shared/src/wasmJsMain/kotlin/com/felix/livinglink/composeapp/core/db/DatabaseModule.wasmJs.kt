package com.felix.livinglink.composeapp.core.db

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.felix.livinglink.composeapp.db.LivingLinkDatabase
import org.koin.core.scope.Scope
import org.w3c.dom.Worker

private fun createSqlWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")

internal actual fun createSqlDriverFactory(scope: Scope): SqlDriverFactory =
    object : SqlDriverFactory {
        override suspend fun create(): SqlDriver {
            val driver = WebWorkerDriver(createSqlWorker())
            LivingLinkDatabase.Schema.awaitCreate(driver)
            return driver
        }
    }