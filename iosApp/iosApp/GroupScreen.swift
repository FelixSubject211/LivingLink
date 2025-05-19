//
//  GroupScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 08.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct GroupScreen: View {
    let groupViewModel: GroupViewModel
    let shoppingListViewModel: ShoppingListViewModel
    let localizables = GroupScreenLocalizables()
    
    var body: some View {
        LoadableStatefulView(
            viewModel: groupViewModel,
            buildAlert: { (error: GroupScreenError) in
                error.asAlert(
                    navigator: groupViewModel.navigator,
                    dismiss: groupViewModel.closeError
                )
            },
            content: content
        )
        .fillMaxSize()
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
    
    private func content(loadableData: GroupViewModel.LoadableData, data: GroupViewModel.Data) -> AnyView {
        ShoppingListScreen(viewModel: shoppingListViewModel)
            .navigationTitle(loadableData.group.name)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(localizables.menuDeleteGroup.localized) {
                            groupViewModel.showDeleteGroupDialog()
                        }
                        Button(localizables.menuCreateInvite.localized) {
                            groupViewModel.createInviteCode()
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .accessibilityLabel(localizables.moreOptionsContentDescription.localized)
                    }
                }
            }
            .alert(
                localizables.groupConfirmDeleteDialogTitle.localized,
                isPresented: .constant(data.showDeleteGroupDialog),
                actions: {
                    Button(localizables.groupConfirmDeleteButton.localized, role: .destructive) {
                        groupViewModel.deleteGroup()
                    }
                    Button(localizables.groupConfirmCancelButton.localized, role: .cancel) {
                        groupViewModel.closeDeleteGroupDialog()
                    }
                },
                message: {
                    Text(localizables.groupConfirmDeleteDialogText.localized)
                }
            )
            .sheet(isPresented: .constant(data.inviteCode != nil, onSetFalse: groupViewModel.closeInviteCode)) {
                VStack(spacing: DesignSystem.Spacing.betweenElements) {
                    Text(localizables.groupInviteDialogTitle.localized)
                        .font(.headline)

                    Text(localizables.groupInviteDialogText.localized)
                        .font(.body)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                    
                    Text(data.inviteCode ?? "")
                        .font(.system(.title, design: .monospaced))
                        .fontWeight(.bold)
                        .padding(.vertical, 8)
                        .padding(.horizontal, 24)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.secondary.opacity(0.1))
                        )
                }
                .padding()
                .presentationDetents([.height(180)])
                .presentationDragIndicator(.visible)
            }
            .eraseToAnyView()
    }
}

fileprivate typealias GroupScreenError = LoadableViewModelStateCombinedError<NetworkError, GroupViewModel.Error, NetworkError>
