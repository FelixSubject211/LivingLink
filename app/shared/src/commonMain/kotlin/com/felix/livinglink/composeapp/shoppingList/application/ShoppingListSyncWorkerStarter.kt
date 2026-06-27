package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.data.ShoppingListSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@Single
class ShoppingListSyncWorkerStarter(
    private val worker: ShoppingListSyncWorker,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val started = AtomicBoolean(false)

    fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return
        scope.launch { worker.run() }
    }
}