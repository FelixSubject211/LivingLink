//
//  RegisterScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 27.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct RegisterScreen: View {
    let viewModel: RegisterViewModel
    let localizables = RegisterScreenLocalizables()
    @FocusState private var focusedField: Field?
    
    var body: some View {
        StatefulView(
            viewModel: viewModel,
            buildAlert: { (error: RegisterScreenError) in
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
    
    private func content(data: RegisterViewModel.Data) -> some View {
        VStack {
            Spacer()
            
            VStack(spacing: 42) {
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
                .submitLabel(.next)
                .onSubmit {
                    focusedField = .confirmPassword
                }
                
                CustomSecureField(
                    label: localizables.confirmPasswordLabel.localized,
                    text: data.confirmPassword,
                    onChange: viewModel.updateConfirmPassword(confirmPassword:)
                )
                .textFieldStyle(DesignSystem.CustomTextFieldStyle())
                .focused($focusedField, equals: .confirmPassword)
                .submitLabel(.go)
                .onSubmit(viewModel.register)
            }

            Spacer()

            Button(
                action: viewModel.register
            ) {
                Text(localizables.registerButtonTitle.localized)
                    .frame(maxWidth: .infinity)
                    .padding()
            }
            .buttonStyle(DesignSystem.PrimaryButtonStyle())
        }.padding(DesignSystem.bodyPadding)
    }
    
    private enum Field {
        case username
        case password
        case confirmPassword
    }
}

fileprivate typealias RegisterScreenError = ViewModelStateCombinedError<RegisterViewModel.Error, NetworkError>
