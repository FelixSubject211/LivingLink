package com.felix.livinglink.composeapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tweener.czan.designsystem.atom.bars.CenterAlignedTopAppBar
import com.tweener.czan.designsystem.atom.button.Button
import com.tweener.czan.designsystem.atom.button.ButtonSize
import com.tweener.czan.designsystem.atom.button.ButtonStyle
import com.tweener.czan.designsystem.atom.scaffold.Scaffold
import com.tweener.czan.theme.Size
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.home_logout_button
import livinglink.app.shared.generated.resources.home_message
import livinglink.app.shared.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
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
                .padding(Size.Padding.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.home_message),
                style = MaterialTheme.typography.bodyLarge,
            )

            Button(
                text = stringResource(Res.string.home_logout_button),
                style = ButtonStyle.PRIMARY,
                size = ButtonSize.BIG,
                onClick = viewModel::onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Size.Padding.Default),
            )
        }
    }
}