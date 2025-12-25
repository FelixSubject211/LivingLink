package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.core.domain.PagingModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun <T> PagingLazyColumn(
    pagingModel: PagingModel<T>?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    loadingContentText: String? = null,
    header: (@Composable () -> Unit)? = null,
    key: (item: T) -> Any,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    if (pagingModel == null) {
        LoadingContent(
            text = loadingContentText,
            loadingProgress = 0.0F
        )
        return
    }

    val listState = rememberLazyListState()
    val pagingState by pagingModel.state.collectAsState(
        initial = PagingModel.State.Loading(0.0F)
    )

    LaunchedEffect(listState, pagingModel, header, pagingState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val dataState = pagingState as? PagingModel.State.Data<T> ?: return@collect
                val itemsLastIndex = dataState.items.lastIndex
                if (itemsLastIndex < 0) {
                    return@collect
                }

                val headerOffset = if (header != null) {
                    1
                } else {
                    0
                }
                val lastIndexInLazyColumn = headerOffset + itemsLastIndex

                if (lastVisibleIndex != null && lastVisibleIndex >= lastIndexInLazyColumn) {
                    pagingModel.loadNextItems()
                }
            }
    }

    when (pagingState) {
        is PagingModel.State.Loading<*> -> {
            val state = (pagingState as? PagingModel.State.Loading<*>)
            LoadingContent(
                text = loadingContentText,
                loadingProgress = state?.progress ?: 0.0F
            )
        }

        is PagingModel.State.Data<*> -> {
            LazyColumn(
                modifier = modifier,
                state = listState,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment
            ) {
                val state = pagingState as? PagingModel.State.Data

                if (header != null) {
                    item(key = "paging-header") {
                        header()
                    }
                }

                items(
                    items = state?.items ?: emptyList(),
                    key = { key(it) }
                ) { item ->
                    itemContent(item)
                }
            }
        }
    }
}