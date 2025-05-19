//
//  SettingsScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct SettingsScreen: View {
    let viewModel: SettingsViewModel
    let localizables = SettingsScreenLocalizables()

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: SettingsScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            content: { loadableData, data in
                SettingsContentScreen(
                    loadableData: loadableData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .navigationTitle(localizables.navigationTitle.localized)
    }
}

private typealias SettingsScreenError = LoadableViewModelStateCombinedError<KotlinNothing, KotlinNothing, NetworkError>
