package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ExecutionScope {
    fun launchJob(block: suspend CoroutineScope.() -> Unit)
    fun <T> launchCollector(
        flow: Flow<T>,
        collector: suspend (T) -> Unit
    )
    fun cancelCurrentJobs()
    fun destroy()
}