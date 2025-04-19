//
//  SettingsScren.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//


import SwiftUI
import ComposeApp

struct SettingsScreen: View {
    let viewModel: SettingsViewModel
    let localizables = SettingsScreenLocalizables()
    
    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: SettingsScreenError) in
                error.asBasicAlert()
            },
            errorContent: { (error: SettingsScreenError) in
                error.asBasicErrorView()
            },
            content: content
        )
        .navigationTitle(localizables.navigationTitle.localized)
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
    
    private func content(loadableData: SettingsViewModel.LoadableData, data: SettingsViewModel.Data) -> some View {
        List {
            Section(localizables.sectionAccountTitle.localized) {
                switch loadableData.session {
                case let loggedIn as AuthenticatedHttpClientAuthSession.LoggedIn:
                    Text(localizables.loggedInAs.localized(loggedIn.username))
                    Button(localizables.logoutButton.localized, action: viewModel.logout)
                    Button(localizables.deleteUserButtonTitle.localized, action: viewModel.showDeleteUserAlert)
                        .alert(
                            isPresented: .constant(data.showDeleteUserAlert, onSetFalse: viewModel.closeDeleteUserAlert)
                        ) {
                            Alert(
                                title: Text(localizables.deleteUserAlertTitle.localized),
                                message: Text(localizables.deleteUserAlertMessage.localized),
                                primaryButton: .default(Text(localizables.deleteUserAlertCancelButton.localized)) {
                                    viewModel.closeDeleteUserAlert()
                                },
                                secondaryButton: .destructive(Text(localizables.deleteUserAlertConfirmButton.localized)) {
                                    viewModel.deleteUser()
                                }
                            )
                        }
                case is AuthenticatedHttpClientAuthSession.LoggedOut:
                    Text(localizables.notLoggedIn.localized)
                    Button(localizables.loginButton.localized, action: viewModel.login)
                default:
                    EmptyView()
                }
            }
            Section(localizables.sectionHapticsTitle.localized) {
                HStack {
                    Text(localizables.enableHaptics.localized)
                    Spacer()
                    Toggle("", isOn: .init(
                        get: {
                            loadableData.hapticsOptions == HapticsSettingsStoreOptions.on
                        },
                        set: { isOn in
                            if isOn {
                                viewModel.setHapticsOption(option: HapticsSettingsStoreOptions.on)
                            } else {
                                viewModel.setHapticsOption(option: HapticsSettingsStoreOptions.off)
                            }
                        }
                    ))
                }
            }
        }
        .scrollContentBackground(.hidden)
        .listStyle(.insetGrouped)
    }
}

fileprivate typealias SettingsScreenError = LoadableViewModelStateCombinedError<KotlinNothing, KotlinNothing, NetworkError>
