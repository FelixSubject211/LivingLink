//
//  RegisterScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 27.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct RegisterScreen: View {
    let viewModel: RegisterViewModel
    let localizables = RegisterScreenLocalizables()

    var body: some View {
        StatefulView(
            viewModel: viewModel,
            buildAlert: { (error: RegisterScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            content: { data in
                RegisterContentScreen(
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .navigationTitle(localizables.navigationTitle.localized)
    }
}

private typealias RegisterScreenError = ViewModelStateCombinedError<RegisterViewModel.Error, NetworkError>
