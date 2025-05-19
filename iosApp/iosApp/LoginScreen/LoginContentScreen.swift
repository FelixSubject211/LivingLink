//
//  LoginContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct LoginContentScreen: View {
    let data: LoginViewModel.Data
    let viewModel: LoginViewModel
    let localizables = LoginScreenLocalizables()
    @FocusState private var focusedField: Field?

    var body: some View {
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

                Button(action: { viewModel.navigator.push(screen: LivingLinkScreen.Register()) }) {
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
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }

    private enum Field {
        case username
        case password
    }
}
