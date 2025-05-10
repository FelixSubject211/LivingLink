package felix.livinglink.ui.common.navigation

import androidx.navigation.NavHostController
import felix.livinglink.event.eventbus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface Navigator {
    fun push(screen: LivingLinkScreen)
    fun pop()
    fun popAll()
    fun addObserver(currentGroupIdObserver: EventBus.CurrentGroupIdObserver)
}

class DefaultNavigator(
    private val navHostController: NavHostController,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : Navigator {

    private var _observer: EventBus.CurrentGroupIdObserver? = null

    override fun push(screen: LivingLinkScreen) {
        scope.launch {
            navHostController.navigate(screen.route) {
                launchSingleTop = true
                restoreState = true
            }
            _observer?.push(screen)
        }
    }

    override fun pop() {
        scope.launch {
            navHostController.navigateUp()
            _observer?.pop()
        }
    }

    override fun popAll() {
        scope.launch {
            navHostController.popBackStack(
                route = navHostController.graph.startDestinationRoute ?: return@launch,
                inclusive = false
            )
            _observer?.popAll()
        }
    }

    override fun addObserver(currentGroupIdObserver: EventBus.CurrentGroupIdObserver) {
        _observer = currentGroupIdObserver
    }
}