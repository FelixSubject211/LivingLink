package com.felix.livinglink.composeapp.ui.shoppinglist.additem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItemSuggestion
import com.felix.livinglink.composeapp.ui.core.atom.SuggestionChip
import com.tweener.czan.theme.Size

@Composable
fun ShoppingListAddItemSuggestionRow(
    suggestions: List<ShoppingListItemSuggestion>,
    visible: Boolean,
    enabled: Boolean,
    onSuggestionClick: (ShoppingListItemSuggestion) -> Unit,
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
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 180),
                        fadeOutSpec = tween(durationMillis = 120),
                        placementSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
                )
            }
        }
    }
}