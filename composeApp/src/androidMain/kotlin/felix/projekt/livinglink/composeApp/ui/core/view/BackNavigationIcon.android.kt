package felix.projekt.livinglink.composeApp.ui.core.view

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import org.jetbrains.compose.resources.DrawableResource

@SuppressLint("ContextCastToActivity")
@Composable
actual fun <A> BackNavigationIcon(
    drawableRes: DrawableResource,
    viewModel: ViewModel<*, A, *>,
    onClickAction: A
) {
    val activity = LocalContext.current as ComponentActivity

    DisposableEffect(activity) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.dispatch(onClickAction)
            }
        }
        activity.onBackPressedDispatcher.addCallback(callback)
        onDispose { callback.remove() }
    }
}