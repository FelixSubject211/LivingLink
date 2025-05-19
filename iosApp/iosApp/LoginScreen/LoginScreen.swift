//
//  LoginScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 23.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct LoginScreen: View {
    let viewModel: LoginViewModel
    let localizables = LoginScreenLocalizables()

    var body: some View {
        StatefulView(
            viewModel: viewModel,
            buildAlert: { (error: LoginScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            content: { data in
                LoginContentScreen(data: data, viewModel: viewModel)
            }
        )
        .navigationTitle(localizables.navigationTitle.localized)
    }
}

private typealias LoginScreenError = ViewModelStateCombinedError<LoginViewModel.Error, NetworkError>
