package felix.livinglink.haptics.controller

import felix.livinglink.haptics.store.HapticsSettingsStore
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual class HapticsDefaultController actual constructor(
    hapticsSettingsStore: HapticsSettingsStore
) : HapticsController {

    private val hapticsSettingsStore = hapticsSettingsStore

    override fun performSuccess() {
        if (hapticsSettingsStore.updates.value?.equals(HapticsSettingsStore.Options.OFF) == true) {
            return
        } else {
            val generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        }
    }

    override fun performError() {
        if (hapticsSettingsStore.updates.value?.equals(HapticsSettingsStore.Options.OFF) == true) {
            return
        } else {
            val generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        }
    }

    override fun performLightImpact() {
        if (hapticsSettingsStore.updates.value?.equals(HapticsSettingsStore.Options.OFF) == true) {
            return
        } else {
            val generator =
                UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            generator.prepare()
            generator.impactOccurred()
        }
    }
}
