package com.felix.livinglink.composeapp.ui.core.molecule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.felix.livinglink.composeapp.ui.core.atom.Checkbox
import com.tweener.czan.theme.Size
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckableListItem(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(loading) {
        if (loading) {
            delay(200)
            showLoading = true
        } else {
            showLoading = false
        }
    }

    val clickable = onClick != null && enabled && !loading
    val longClickable = onLongClick != null && enabled && !loading

    val rowModifier =
        if (longClickable) {
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                    onLongClick = { onLongClick?.invoke() },
                )
        } else {
            modifier.fillMaxWidth()
        }

    Row(
        modifier = rowModifier
            .padding(horizontal = Size.Padding.Default, vertical = Size.Padding.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Size.Padding.Default),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (checked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration =
                if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Checkbox(
                    checked = checked,
                    enabled = clickable,
                    onCheckedChange = {
                        if (clickable) onClick.invoke()
                    },
                )
            }
        }
    }
}