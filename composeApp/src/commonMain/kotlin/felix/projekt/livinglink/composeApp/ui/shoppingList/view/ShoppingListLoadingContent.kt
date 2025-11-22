package felix.projekt.livinglink.composeApp.ui.shoppingList.view

import ShoppingListLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListState

@Composable
fun ShoppingListLoadingContent(
    state: ShoppingListState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ShoppingListLocalizables.LoadingShoppingList())

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.loadingProgress == 0f) {
                CircularProgressIndicator()
            } else {
                LinearProgressIndicator(
                    progress = { state.loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )
            }
        }
    }
}
