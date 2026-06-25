package com.felix.livinglink.composeapp.core.db

import app.cash.sqldelight.db.SqlDriver
import com.felix.livinglink.composeapp.db.LivingLinkDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

interface SqlDriverFactory {
    suspend fun create(): SqlDriver
}

@Single
fun provideSqlDriverFactory(scope: Scope): SqlDriverFactory = createSqlDriverFactory(scope)

internal expect fun createSqlDriverFactory(scope: Scope): SqlDriverFactory

@Single
class DatabaseProvider(
    private val factory: SqlDriverFactory,
) {
    private val mutex = Mutex()
    private var database: LivingLinkDatabase? = null

    suspend fun get(): LivingLinkDatabase =
        database ?: mutex.withLock {
            database ?: LivingLinkDatabase(factory.create()).also { database = it }
        }
}