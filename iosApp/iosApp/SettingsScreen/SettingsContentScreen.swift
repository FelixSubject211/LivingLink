//
//  SettingsContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct SettingsContentScreen: View {
    let loadableData: SettingsViewModel.LoadableData
    let data: SettingsViewModel.Data
    let viewModel: SettingsViewModel
    let localizables = SettingsScreenLocalizables()

    var body: some View {
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
                    Button(localizables.loginButton.localized, action: { viewModel.navigator.push(screen: LivingLinkScreen.Login()) })
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
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
}
