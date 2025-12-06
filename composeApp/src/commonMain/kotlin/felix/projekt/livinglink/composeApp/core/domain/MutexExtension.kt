package felix.projekt.livinglink.composeApp.core.domain

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <T> Mutex.withLockNonSuspend(action: () -> T): T = runBlocking {
    return@runBlocking this@withLockNonSuspend.withLock(action = action)
}