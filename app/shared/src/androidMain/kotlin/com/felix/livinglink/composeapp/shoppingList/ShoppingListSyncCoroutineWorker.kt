package com.felix.livinglink.composeapp.shoppingList

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.felix.livinglink.composeapp.di.LivingLinkClientModule
import com.felix.livinglink.composeapp.shoppingList.data.ShoppingListSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.modules
import java.util.concurrent.TimeUnit

class ShoppingListSyncCoroutineWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val syncWorker: ShoppingListSyncWorker by inject()

    override suspend fun doWork(): Result =
        try {
            ensureKoin(appContext)
            if (syncWorker.syncOnce()) Result.success() else Result.retry()
        } catch (_: Throwable) {
            Result.retry()
        }

    private fun ensureKoin(context: Context) {
        if (GlobalContext.getOrNull() != null) return
        startKoin {
            androidContext(context.applicationContext)
            modules(LivingLinkClientModule::class)
        }
    }
}

fun scheduleShoppingListSync(context: Context) {
    val request = PeriodicWorkRequestBuilder<ShoppingListSyncCoroutineWorker>(
        15, TimeUnit.MINUTES,
    ).setConstraints(
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "shopping-list-sync",
        ExistingPeriodicWorkPolicy.KEEP,
        request,
    )
}