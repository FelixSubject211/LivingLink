package felix.livinglink.haptics

import felix.livinglink.common.CommonModule
import felix.livinglink.haptics.controller.HapticsController
import felix.livinglink.haptics.controller.HapticsDefaultController
import felix.livinglink.haptics.store.HapticsSettingsDefaultStore
import felix.livinglink.haptics.store.HapticsSettingsStore

interface HapticsModule {
    val hapticsSettingsStore: HapticsSettingsStore
    val hapticsController: HapticsController
}

fun defaultHapticsModule(
    commonModule: CommonModule
): HapticsModule {

    val hapticsSettingsStore = HapticsSettingsDefaultStore(
        scope = commonModule.defaultScope
    )

    val hapticsController = HapticsDefaultController(
        hapticsSettingsStore = hapticsSettingsStore
    )

    return object : HapticsModule {
        override val hapticsSettingsStore = hapticsSettingsStore
        override val hapticsController = hapticsController
    }
}