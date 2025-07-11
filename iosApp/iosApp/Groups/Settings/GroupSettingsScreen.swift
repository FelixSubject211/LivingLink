//
//  GroupSettingsScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 11.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupSettingsScreen: View {
    let viewModel: GroupSettingsViewModel
    let localizables = GroupsSettingsScreenLocalizables()

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: GroupSettingsScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            content: { loadbaleData, data in
                GroupSettingsContentScreen(
                    loadableData: loadbaleData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .fillMaxSize()
    }
}

private typealias GroupSettingsScreenError = LoadableViewModelStateCombinedError<LivingLinkError, KotlinNothing, NetworkError>
