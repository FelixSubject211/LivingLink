package com.felix.livinglink.composeapp.core.db

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.felix.livinglink.composeapp.db.LivingLinkDatabase
import org.koin.core.scope.Scope

internal actual fun createSqlDriverFactory(scope: Scope): SqlDriverFactory =
    object : SqlDriverFactory {
        override suspend fun create(): SqlDriver =
            AndroidSqliteDriver(
                schema = LivingLinkDatabase.Companion.Schema.synchronous(),
                context = scope.get<Context>(),
                name = "livinglink.db",
            )
    }