//
//  RegisterContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct RegisterContentScreen: View {
    let data: RegisterViewModel.Data
    let viewModel: RegisterViewModel
    let localizables = RegisterScreenLocalizables()
    @FocusState private var focusedField: Field?

    var body: some View {
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

            DesignSystem.PrimaryButton(
                title: localizables.registerButtonTitle.localized,
                action: viewModel.register
            )
        }
        .padding(DesignSystem.Padding.large)
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }

    private enum Field {
        case username
        case password
        case confirmPassword
    }
}
