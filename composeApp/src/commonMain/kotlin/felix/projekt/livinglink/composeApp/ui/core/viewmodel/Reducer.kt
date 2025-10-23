package felix.projekt.livinglink.composeApp.ui.core.viewmodel

interface Reducer<STATE, RESULT> {
    operator fun invoke(state: STATE, result: RESULT): STATE
}