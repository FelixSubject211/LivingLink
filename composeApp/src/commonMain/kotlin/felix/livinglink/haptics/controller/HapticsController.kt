package felix.livinglink.haptics.controller

import felix.livinglink.haptics.store.HapticsSettingsStore

interface HapticsController {
    fun performSuccess()
    fun performError()
    fun performLightImpact()
}

expect class HapticsDefaultController(
    hapticsSettingsStore: HapticsSettingsStore
) : HapticsController