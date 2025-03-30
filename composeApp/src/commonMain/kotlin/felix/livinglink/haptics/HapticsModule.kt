package felix.livinglink.haptics

interface HapticsModule {
    val hapticsController: HapticsController
}

fun defaultHapticsModule(): HapticsModule {
    return object : HapticsModule {
        override val hapticsController = HapticsDefaultController()
    }
}