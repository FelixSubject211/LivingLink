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
            Section(localizables.sectionMembersTitle.localized) {
                ForEach(loadableData.group.groupMembersSortedByRoleAndName, id: \.self) { member in
                    HStack {
                        Text(member.name)

                        Spacer()

                        if member.isAdmin {
                            Text(localizables.adminSuffix.localized)
                                .foregroundColor(DesignSystem.Colors.labelColor)
                                .font(.caption)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(DesignSystem.Colors.labelColor.opacity(0.2))
                                .clipShape(Capsule())
                        }
                    }
                }
            }
            Section(localizables.sectionGeneralGroupTitle.localized) {
                let isAdmin = loadableData.group.adminUserIds.contains(loadableData.currentUserId)

                if isAdmin {
                    Button(localizables.createInvite.localized, action: viewModel.createInviteCode)
                        .sheet(
                            isPresented: .constant(data.inviteCode != nil, onSetFalse: viewModel.closeInviteCode)
                        ) {
                            if let code = data.inviteCode {
                                GroupInviteCodeSheet(inviteCode: code)
                            }
                        }
                }
                Button(localizables.leaveGroup.localized, role: .destructive, action: viewModel.showLeaveGroupDialog)
                    .modifier(
                        GroupLeaveConfirmationAlert(
                            isPresented: data.showLeaveGroupDialog,
                            onConfirm: viewModel.leaveGroup,
                            onCancel: viewModel.closeLeaveGroupDialog
                        )
                    )
                if isAdmin {
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
        }
        .scrollContentBackground(.hidden)
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
}
