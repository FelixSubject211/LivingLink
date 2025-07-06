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
                placeholder: localizables.createGroupDialogLabel.localized,
                confirmTitle: localizables.createGroupDialogConfirm.localized,
                cancelTitle: localizables.createGroupDialogCancel.localized,
                onCancel: viewModel.closeAddGroupDialog,
                onConfirm: viewModel.createGroup(groupName:)
            )
            .alertWithTextField(
                title: localizables.joinGroupDialogTitle.localized,
                message: localizables.joinGroupDialogLabel.localized,
                isPresented: data.showJoinGroupDialog,
                placeholder: localizables.joinGroupDialogLabel.localized,
                confirmTitle: localizables.joinGroupDialogConfirm.localized,
                cancelTitle: localizables.joinGroupDialogCancel.localized,
                onCancel: viewModel.closeJoinGroupDialog,
                onConfirm: viewModel.useInvite(code:)
            )
    }
}
