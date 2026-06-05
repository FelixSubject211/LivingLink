package com.felix.livinglink.server.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import kotlin.test.Test

class LayerArchitectureTest {
    private val domain = Layer("Domain", "com.felix.livinglink..domain..")
    private val application = Layer("Application", "com.felix.livinglink..application..")
    private val infrastructure = Layer("Infrastructure", "com.felix.livinglink..infrastructure..")
    private val delivery = Layer("Delivery", "com.felix.livinglink..delivery..")

    @Test
    fun `clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                domain.dependsOnNothing()

                application.dependsOn(domain)
                application.doesNotDependOn(infrastructure, delivery)

                infrastructure.dependsOn(domain, application)
                infrastructure.doesNotDependOn(delivery)

                delivery.dependsOn(domain, application)
                delivery.doesNotDependOn(infrastructure)
            }
    }
}
