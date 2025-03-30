package felix.livinglink.haptics

interface HapticsController {
    fun performSuccess()
    fun performError()
    fun performLightImpact()
}

expect class HapticsDefaultController() : HapticsController