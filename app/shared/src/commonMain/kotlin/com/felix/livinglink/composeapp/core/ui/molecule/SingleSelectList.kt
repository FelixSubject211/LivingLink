package com.felix.livinglink.composeapp.core.ui.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.felix.livinglink.composeapp.core.ui.atom.SelectableListItem
import com.tweener.czan.theme.Size

@Composable
fun <T> SingleSelectList(
    items: List<T>,
    selectedKey: Any,
    key: (T) -> Any,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, selected: Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Size.Padding.Small),
    ) {
        items.forEach { item ->
            val selected = key(item) == selectedKey
            SelectableListItem(
                selected = selected,
                onClick = { onSelect(item) },
            ) {
                itemContent(item, selected)
            }
        }
    }
}