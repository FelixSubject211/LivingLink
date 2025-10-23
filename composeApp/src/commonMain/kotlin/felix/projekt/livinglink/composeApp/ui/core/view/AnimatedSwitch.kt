package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.runtime.Composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedSwitch(
    targetState: S,
    isForward: Boolean,
    content: @Composable (S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            if (isForward) {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() with
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        }
    ) { target ->
        content(target)
    }
}