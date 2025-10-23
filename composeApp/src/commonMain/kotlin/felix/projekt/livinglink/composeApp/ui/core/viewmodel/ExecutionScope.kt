package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import kotlinx.coroutines.CoroutineScope

interface ExecutionScope {
    fun launchJob(block: suspend CoroutineScope.() -> Unit)
    fun cancelCurrentJobs()
    fun destroy()
}