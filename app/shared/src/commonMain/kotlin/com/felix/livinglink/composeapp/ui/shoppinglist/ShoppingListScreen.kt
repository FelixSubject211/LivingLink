package com.felix.livinglink.composeapp.ui.shoppinglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felix.livinglink.composeapp.ui.core.atom.CheckableListItem
import com.felix.livinglink.composeapp.ui.core.molecule.VisibleRangeEffect
import com.felix.livinglink.composeapp.ui.core.organism.ErrorContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.tweener.czan.designsystem.atom.bars.CenterAlignedTopAppBar
import com.tweener.czan.designsystem.atom.line.HorizontalDashedLine
import com.tweener.czan.designsystem.atom.line.LineDefaults
import com.tweener.czan.designsystem.atom.scaffold.Scaffold
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.network_error_title
import livinglink.app.shared.generated.resources.shopping_list_error_network_description
import livinglink.app.shared.generated.resources.shopping_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = stringResource(Res.string.shopping_list_title),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (val current = state.value) {
                is ShoppingListScreenState.Loading ->
                    CircularProgressIndicator()

                is ShoppingListScreenState.Error ->
                    ErrorContent(
                        title = stringResource(Res.string.network_error_title),
                        description = stringResource(Res.string.shopping_list_error_network_description),
                    )

                is ShoppingListScreenState.Content ->
                    ShoppingListContent(
                        shoppingList = current.shoppingList,
                        listState = listState,
                        onVisibleRangeChanged = viewModel::onVisibleRangeChanged,
                    )
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    shoppingList: ShoppingListContent,
    listState: LazyListState,
    onVisibleRangeChanged: (first: Int, last: Int) -> Unit,
) {
    VisibleRangeEffect(
        listState = listState,
        onVisibleRangeChanged = onVisibleRangeChanged,
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Divider()
        }
        items(
            count = shoppingList.totalCount,
            key = { index -> shoppingList.order[index] ?: "placeholder-$index" },
        ) { index ->
            val item = shoppingList.itemAt(index)
            CheckableListItem(
                text = item?.name.orEmpty(),
                checked = item?.completed ?: false,
                enabled = false,
                onClick = null,
            )
            Divider()
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDashedLine(
        modifier = Modifier.fillMaxWidth(),
        colors = LineDefaults.lineColors(
            strokeColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}