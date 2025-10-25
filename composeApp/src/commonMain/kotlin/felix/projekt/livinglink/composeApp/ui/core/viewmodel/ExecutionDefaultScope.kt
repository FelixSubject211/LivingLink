package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExecutionDefaultScope(
    private val parentScope: CoroutineScope,
    private val lifecycle: Lifecycle,
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

    override fun <T> launchCollector(
        flow: Flow<T>,
        collector: suspend (T) -> Unit
    ) {
        launchJob {
            flow.flowWithLifecycle(
                lifecycle = lifecycle,
                minActiveState = Lifecycle.State.STARTED
            ).collect { value ->
                collector(value)
            }
        }
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