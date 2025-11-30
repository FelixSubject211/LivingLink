package felix.projekt.livinglink.composeApp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.rememberNavController
import de.comahe.i18n4k.config.I18n4kConfigImmutable
import de.comahe.i18n4k.i18n4kInitCldrPluralRules
import felix.projekt.livinglink.composeApp.auth.di.authModule
import felix.projekt.livinglink.composeApp.core.di.coreModule
import felix.projekt.livinglink.composeApp.eventSourcing.di.eventSourcingModule
import felix.projekt.livinglink.composeApp.groups.di.groupsModule
import felix.projekt.livinglink.composeApp.shoppingList.di.shoppingListModule
import felix.projekt.livinglink.composeApp.ui.core.view.customDarkScheme
import felix.projekt.livinglink.composeApp.ui.navigation.NavigationHost
import felix.projekt.livinglink.composeApp.ui.navigation.viewModelModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App() {
    Napier.base(DebugAntilog())

    val i18n4kConfig = mutableStateOf(I18n4kConfigImmutable())
    i18n4kInitCldrPluralRules()
    val navHostController = rememberNavController()

    KoinApplication(application = {
        modules(
            listOf(
                coreModule,
                authModule,
                groupsModule,
                eventSourcingModule,
                shoppingListModule,
                viewModelModule
            )
        )
    }) {
        key(i18n4kConfig) {
            val isDarkTheme = isSystemInDarkTheme()
            val colorScheme = if (isDarkTheme) {
                customDarkScheme
            } else {
                lightColorScheme()
            }

            MaterialExpressiveTheme(colorScheme) {
                NavigationHost(navController = navHostController)
            }
        }
    }
}
