package com.felix.livinglink.composeapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felix.livinglink.composeapp.ui.core.molecule.SelectableListItem
import com.felix.livinglink.composeapp.ui.core.organism.SingleSelectList
import com.tweener.czan.designsystem.atom.bars.CenterAlignedTopAppBar
import com.tweener.czan.designsystem.atom.button.Button
import com.tweener.czan.designsystem.atom.button.ButtonStyle
import com.tweener.czan.designsystem.atom.scaffold.Scaffold
import com.tweener.czan.designsystem.atom.text.Text
import com.tweener.czan.designsystem.organism.card.Card
import com.tweener.czan.designsystem.organism.card.CardDefaults
import com.tweener.czan.theme.Size
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.home_error_network
import livinglink.app.shared.generated.resources.home_groups_empty
import livinglink.app.shared.generated.resources.home_groups_single_subtitle
import livinglink.app.shared.generated.resources.home_groups_subtitle
import livinglink.app.shared.generated.resources.home_groups_title
import livinglink.app.shared.generated.resources.home_logged_in_as
import livinglink.app.shared.generated.resources.home_logout_button
import livinglink.app.shared.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = stringResource(Res.string.home_title),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Size.Padding.Default),
            verticalArrangement = Arrangement.spacedBy(Size.Padding.Large),
        ) {
            AccountCard(
                username = state.value.username,
                onLogout = viewModel::onLogout,
            )

            GroupsSection(
                groupsState = state.value.groups,
                onSelectGroup = viewModel::onSelectGroup,
            )
        }
    }
}

@Composable
private fun AccountCard(
    username: String?,
    onLogout: () -> Unit,
) {
    Card(
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Size.Padding.Default),
        ) {
            Text(
                text = stringResource(Res.string.home_logged_in_as, username ?: "…"),
                style = MaterialTheme.typography.bodyLarge,
            )

            Button(
                text = stringResource(Res.string.home_logout_button),
                style = ButtonStyle.PRIMARY,
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GroupsSection(
    groupsState: GroupsUiState,
    onSelectGroup: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Size.Padding.Default),
    ) {
        Text(
            text = stringResource(Res.string.home_groups_title),
            style = MaterialTheme.typography.titleMedium,
        )

        val subtitle =
            when (groupsState) {
                is GroupsUiState.Content -> stringResource(Res.string.home_groups_subtitle)
                is GroupsUiState.Single -> stringResource(Res.string.home_groups_single_subtitle)
                else -> null
            }

        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (val state = groupsState) {
            is GroupsUiState.Loading ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is GroupsUiState.Error ->
                Text(
                    text = stringResource(Res.string.home_error_network),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )

            is GroupsUiState.Empty ->
                Text(
                    text = stringResource(Res.string.home_groups_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

            is GroupsUiState.Single ->
                SelectableListItem(
                    selected = false,
                ) {
                    Text(
                        text = state.group.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

            is GroupsUiState.Content ->
                SingleSelectList(
                    items = state.groups,
                    selectedKey = state.selectedGroupId,
                    key = { it.id },
                    onSelect = { onSelectGroup(it.id) },
                ) { group, selected ->
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
        }
    }
}