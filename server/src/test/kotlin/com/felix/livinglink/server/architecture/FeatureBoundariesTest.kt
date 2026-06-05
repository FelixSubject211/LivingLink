package com.felix.livinglink.server.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class FeatureBoundariesTest {
    private val core = Layer("Core", "com.felix.livinglink.server.core..")
    private val user = Layer("User", "com.felix.livinglink.server.user..")
    private val shoppingList = Layer("ShoppingList", "com.felix.livinglink.server.shoppingList..")
    private val calendar = Layer("Calendar", "com.felix.livinglink.server.calendar..")
    private val session = Layer("Session", "com.felix.livinglink.server.session..")

    @Test
    fun `feature dependencies are correct`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                core.dependsOnNothing()

                user.dependsOn(core)
                user.doesNotDependOn(shoppingList, calendar, session)

                shoppingList.dependsOn(core, user)
                shoppingList.doesNotDependOn(calendar, session)

                calendar.dependsOn(core, user)
                calendar.doesNotDependOn(shoppingList, session)

                session.dependsOn(core, user, shoppingList, calendar)
            }
    }

    @Test
    fun `features do not access user config or domain`() {
        listOf(
            "session",
            "shoppingList",
            "calendar",
        ).forEach { feature ->
            assertNoImportsFrom(
                featurePackagePrefix = "com.felix.livinglink.server.$feature",
                forbiddenImportPrefix = "com.felix.livinglink.server.user.config",
            )

            assertNoImportsFrom(
                featurePackagePrefix = "com.felix.livinglink.server.$feature",
                forbiddenImportPrefix = "com.felix.livinglink.server.user.domain",
            )
        }
    }

    @Test
    fun `session only accesses shoppingList and calendar applications`() {
        listOf(
            "shoppingList",
            "calendar",
        ).forEach { feature ->
            assertNoImportsFrom(
                featurePackagePrefix = "com.felix.livinglink.server.session",
                forbiddenImportPrefix = "com.felix.livinglink.server.$feature.domain",
            )

            assertNoImportsFrom(
                featurePackagePrefix = "com.felix.livinglink.server.session",
                forbiddenImportPrefix = "com.felix.livinglink.server.$feature.infrastructure",
            )

            assertNoImportsFrom(
                featurePackagePrefix = "com.felix.livinglink.server.session",
                forbiddenImportPrefix = "com.felix.livinglink.server.$feature.delivery",
            )
        }
    }

    private fun assertNoImportsFrom(
        featurePackagePrefix: String,
        forbiddenImportPrefix: String,
    ) {
        Konsist
            .scopeFromProduction()
            .files
            .filter { it.packagee?.name?.startsWith(featurePackagePrefix) == true }
            .assertTrue { file ->
                file.imports.none { it.name.startsWith(forbiddenImportPrefix) }
            }
    }
}
