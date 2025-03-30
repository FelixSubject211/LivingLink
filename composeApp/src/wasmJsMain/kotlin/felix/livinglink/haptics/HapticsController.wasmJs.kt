package felix.livinglink.haptics

actual class HapticsDefaultController : HapticsController {
    override fun performSuccess() {}

    override fun performError() {}

    override fun performLightImpact() {}
}