//
//  GroupSettingsContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 11.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupSettingsContentScreen: View {
    let loadableData: GroupSettingsViewModel.LoadableData
    let data: GroupSettingsViewModel.Data
    let viewModel: GroupSettingsViewModel
    let localizables = GroupsSettingsScreenLocalizables()

    var body: some View {
        List {
            Section(localizables.sectionGeneralGroupTitle.localized) {
                Button(localizables.createInvite.localized, action: viewModel.createInviteCode)
                    .sheet(
                        isPresented: .constant(data.inviteCode != nil, onSetFalse: viewModel.closeInviteCode)
                    ) {
                        if let code = data.inviteCode {
                            GroupInviteCodeSheet(inviteCode: code)
                        }
                    }
                Button(localizables.deleteGroup.localized, role: .destructive, action: viewModel.showDeleteGroupDialog)
                    .modifier(
                        GroupDeleteConfirmationAlert(
                            isPresented: data.showDeleteGroupDialog,
                            onConfirm: viewModel.deleteGroup,
                            onCancel: viewModel.closeDeleteGroupDialog
                        )
                    )
            }
        }
        .scrollContentBackground(.hidden)
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
}
