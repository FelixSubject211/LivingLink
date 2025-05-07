package felix.livinglink.haptics.store

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.store.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

interface HapticsSettingsStore {

    val updates: StateFlow<Options?>

    suspend fun set(option: Options): LivingLinkResult<Unit, Nothing>

    @Serializable
    enum class Options {
        ON,
        OFF
    }
}

class HapticsSettingsDefaultStore(
    scope: CoroutineScope
) : HapticsSettingsStore {

    val store = createStore(
        path = "HapticsSettingsStore",
        defaultValue = HapticsSettingsStore.Options.ON
    )

    override val updates = store.updates.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    override suspend fun set(
        option: HapticsSettingsStore.Options
    ): LivingLinkResult<Unit, Nothing> {
        store.set(option)
        return LivingLinkResult.Success(Unit)
    }
}