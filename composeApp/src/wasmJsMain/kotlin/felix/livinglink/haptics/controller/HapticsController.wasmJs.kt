package felix.livinglink.haptics.controller

import felix.livinglink.haptics.store.HapticsSettingsStore

actual class HapticsDefaultController actual constructor(
    hapticsSettingsStore: HapticsSettingsStore
) : HapticsController {
    override fun performSuccess() {}

    override fun performError() {}

    override fun performLightImpact() {}
}