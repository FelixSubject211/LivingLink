//
//  GroupDetailContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupDetailContentScreen: View {
    let loadableData: GroupDetailViewModel.LoadableData
    let data: GroupDetailViewModel.Data
    let viewModel: GroupDetailViewModel
    let shoppingListViewModel: ShoppingListListViewModel
    let taskBoardListViewModel: TaskBoardListViewModel
    let localizables = GroupDetailScreenLocalizables()

    var body: some View {
        TabView {
            ShoppingListListScreen(viewModel: shoppingListViewModel)
                .tabItem {
                    Label(localizables.tabShoppingList.localized, systemImage: "cart")
                }

            TaskBoardListScreen(viewModel: taskBoardListViewModel)
                .tabItem {
                    Label(localizables.tabTaskBoard.localized, systemImage: "checkmark.square")
                }
        }
        .navigationTitle(loadableData.group.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    Button(localizables.menuDeleteGroup.localized) {
                        viewModel.showDeleteGroupDialog()
                    }
                    Button(localizables.menuCreateInvite.localized) {
                        viewModel.createInviteCode()
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .accessibilityLabel(localizables.moreOptionsContentDescription.localized)
                }
            }
        }
        .modifier(
            GroupDeleteConfirmationAlert(
                isPresented: data.showDeleteGroupDialog,
                onConfirm: viewModel.deleteGroup,
                onCancel: viewModel.closeDeleteGroupDialog
            )
        )
        .sheet(
            isPresented: .constant(data.inviteCode != nil, onSetFalse: viewModel.closeInviteCode)
        ) {
            if let code = data.inviteCode {
                GroupInviteCodeSheet(inviteCode: code)
            }
        }
    }
}
