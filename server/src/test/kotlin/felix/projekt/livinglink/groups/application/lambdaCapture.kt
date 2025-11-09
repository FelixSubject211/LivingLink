package felix.projekt.livinglink.groups.application

import dev.mokkery.matcher.capture.Capture
import felix.projekt.livinglink.server.groups.domain.Group

inline fun <reified R> lambdaCapture() = Capture.container<(Group) -> R>()