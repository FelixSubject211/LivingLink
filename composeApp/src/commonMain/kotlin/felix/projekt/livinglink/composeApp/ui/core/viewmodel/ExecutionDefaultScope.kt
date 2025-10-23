package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ExecutionDefaultScope(
    parentScope: CoroutineScope
) : ExecutionScope {
    private val job = SupervisorJob(parentScope.coroutineContext[Job])
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val activeJobs = mutableSetOf<Job>()

    override fun launchJob(block: suspend CoroutineScope.() -> Unit) {
        val job = scope.launch {
            try {
                block()
            } catch (_: CancellationException) {
            }
        }
        activeJobs += job
        job.invokeOnCompletion { activeJobs -= job }
    }

    override fun cancelCurrentJobs() {
        activeJobs.forEach { it.cancel() }
        activeJobs.clear()
    }

    override fun destroy() {
        cancelCurrentJobs()
        scope.cancel()
    }
}