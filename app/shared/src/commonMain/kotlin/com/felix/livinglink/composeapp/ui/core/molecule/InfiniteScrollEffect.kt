package com.felix.livinglink.composeapp.ui.core.molecule

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun VisibleRangeEffect(
    listState: LazyListState,
    onVisibleRangeChanged: (first: Int, last: Int) -> Unit,
) {
    val current = rememberUpdatedState(onVisibleRangeChanged)

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo.visibleItemsInfo
            if (info.isEmpty()) {
                null
            } else {
                info.first().index to info.last().index
            }
        }
            .distinctUntilChanged()
            .collect { range ->
                if (range != null) {
                    current.value(range.first, range.second)
                }
            }
    }
}