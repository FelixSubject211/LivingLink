package com.felix.livinglink.composeapp.ui.core.molecule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion
import com.felix.livinglink.composeapp.ui.core.atom.SuggestionChip
import com.tweener.czan.theme.Size

@Composable
fun SuggestionRow(
    suggestions: List<ItemSuggestion>,
    visible: Boolean,
    enabled: Boolean,
    onSuggestionClick: (ItemSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(suggestions) {
        listState.scrollToItem(0)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Size.Padding.Small),
            contentPadding = PaddingValues(
                horizontal = Size.Padding.Default,
                vertical = 4.dp,
            ),
        ) {
            items(
                items = suggestions,
                key = { it.name },
            ) { suggestion ->
                SuggestionChip(
                    text = suggestion.name,
                    enabled = enabled,
                    onClick = { onSuggestionClick(suggestion) },
                )
            }
        }
    }
}