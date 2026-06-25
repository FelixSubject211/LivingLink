package com.felix.livinglink.composeapp.core.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.felix.livinglink.composeapp.db.LivingLinkDatabase
import org.koin.core.scope.Scope

internal actual fun createSqlDriverFactory(scope: Scope): SqlDriverFactory =
    object : SqlDriverFactory {
        override suspend fun create(): SqlDriver =
            NativeSqliteDriver(
                schema = LivingLinkDatabase.Companion.Schema.synchronous(),
                name = "livinglink.db",
            )
    }