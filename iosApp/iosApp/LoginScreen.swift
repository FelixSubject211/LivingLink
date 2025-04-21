//
//  LoginScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 23.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct LoginScreen: View {
    let viewModel: LoginViewModel
    let localizables = LoginScreenLocalizables()
    @FocusState private var focusedField: Field?
    
    var body: some View {
        StatefulView(
            viewModel: viewModel,
            buildAlert: { (error: LoginScreenError) in
                error.asBasicAlert()
            },
            content: content(data:)
        )
        .navigationTitle(localizables.navigationTitle.localized)
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
    
    private func content(data: LoginViewModel.Data) -> AnyView {
        VStack {
            Spacer()
            
            VStack(spacing: 56) {
                CustomTextField(
                    label: localizables.usernameLabel.localized,
                    text: data.username,
                    onChange: viewModel.updateUsername(username:)
                )
                .textFieldStyle(DesignSystem.CustomTextFieldStyle())
                .focused($focusedField, equals: .username)
                .submitLabel(.next)
                .onSubmit {
                    focusedField = .password
                }

                CustomSecureField(
                    label: localizables.passwordLabel.localized,
                    text: data.password,
                    onChange: viewModel.updatePassword(password:)
                )
                .textFieldStyle(DesignSystem.CustomTextFieldStyle())
                .focused($focusedField, equals: .password)
                .submitLabel(.go)
                .onSubmit(viewModel.login)

                Button(action: viewModel.register) {
                    Text(localizables.registerHintText.localized)
                        .font(.footnote)
                }
            }

            Spacer()

            DesignSystem.PrimaryButton(
                title: localizables.loginButtonTitle.localized,
                action: viewModel.login
            )
        }
        .padding(DesignSystem.Padding.large)
        .eraseToAnyView()
    }
    
    private enum Field {
        case username
        case password
    }
}

fileprivate typealias LoginScreenError = ViewModelStateCombinedError<LoginViewModel.Error, NetworkError>
