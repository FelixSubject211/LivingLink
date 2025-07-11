//
//  GroupListScaffoldModifier.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupListScaffoldModifier: ViewModifier {
    let data: GroupListViewModel.Data
    let viewModel: GroupListViewModel
    let localizables = GroupListScreenLocalizables()

    func body(content: Content) -> some View {
        content
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }
            .modifier(
                GroupDeleteConfirmationAlert(
                    isPresented: data.groupIdToDelete != nil,
                    onConfirm: viewModel.deleteGroup,
                    onCancel: viewModel.closeDeleteDialog
                )
            )
            .alertWithTextField(
                title: localizables.createGroupDialogTitle.localized,
                message: localizables.createGroupDialogLabel.localized,
                isPresented: data.showAddGroupDialog,
                text: data.addGroupName,
                onTextChange: viewModel.updateAddGroupName(groupName:),
                placeholder: localizables.createGroupDialogLabel.localized,
                confirmButtonTitle: localizables.createGroupDialogConfirm.localized,
                cancelButtonTitle: localizables.createGroupDialogCancel.localized,
                isConfirmButtonEnabled: viewModel.createGroupConfirmButtonEnabled(),
                onCancel: viewModel.closeAddGroupDialog,
                onConfirm: viewModel.createGroup
            )
            .alertWithTextField(
                title: localizables.joinGroupDialogTitle.localized,
                message: localizables.joinGroupDialogLabel.localized,
                isPresented: data.showJoinGroupDialog,
                text: data.inviteCode,
                onTextChange: viewModel.updateInviteCode(inviteCode:),
                placeholder: localizables.joinGroupDialogLabel.localized,
                confirmButtonTitle: localizables.joinGroupDialogConfirm.localized,
                cancelButtonTitle: localizables.joinGroupDialogCancel.localized,
                isConfirmButtonEnabled: viewModel.useInviteConfirmButtonEnabled(),
                onCancel: viewModel.closeJoinGroupDialog,
                onConfirm: viewModel.useInvite
            )
    }
}
