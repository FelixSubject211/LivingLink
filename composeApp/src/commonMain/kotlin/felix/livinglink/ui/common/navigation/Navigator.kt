package felix.livinglink.ui.common.navigation

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface Navigator {
    fun push(screen: LivingLinkScreen)
    fun pop()
    fun popAll()
}

class DefaultNavigator(
    private val navHostController: NavHostController,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : Navigator {

    override fun push(screen: LivingLinkScreen) {
        scope.launch {
            navHostController.navigate(screen.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    override fun pop() {
        scope.launch { navHostController.navigateUp() }
    }

    override fun popAll() {
        scope.launch {
            navHostController.popBackStack(
                route = navHostController.graph.startDestinationRoute ?: return@launch,
                inclusive = false
            )
        }
    }
}