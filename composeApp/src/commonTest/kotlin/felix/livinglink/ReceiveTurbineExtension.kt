package felix.livinglink

import app.cash.turbine.ReceiveTurbine
import felix.livinglink.ui.common.state.LoadableViewModelState
import kotlin.test.assertEquals
import kotlin.test.assertIs

suspend fun <T : Any, E : Any> ReceiveTurbine<LoadableViewModelState.State<T, E>>.expectStates(
    vararg expected: LoadableViewModelState.State<T, E>
) {
    expected.forEach { expectedState ->
        val actual = awaitItem()

        when (expectedState) {
            is LoadableViewModelState.State.Loading -> {
                assertIs<LoadableViewModelState.State.Loading<*, *>>(actual)
            }

            is LoadableViewModelState.State.Empty -> {
                assertIs<LoadableViewModelState.State.Empty<*, *>>(actual)
            }

            is LoadableViewModelState.State.Data -> {
                assertIs<LoadableViewModelState.State.Data<*, *>>(actual)
                assertEquals(
                    expectedState.data,
                    (actual as LoadableViewModelState.State.Data<*, *>).data
                )
            }
        }
    }
}


