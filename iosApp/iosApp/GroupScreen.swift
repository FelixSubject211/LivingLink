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
    let viewModel: GroupViewModel
    let localizables = GroupScreenLocalizables()
    
    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: GroupScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
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
        Text(loadableData.description())
            .navigationTitle(loadableData.group.name)
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
            .alert(
                localizables.groupConfirmDeleteDialogTitle.localized,
                isPresented: .constant(data.showDeleteGroupDialog),
                actions: {
                    Button(localizables.groupConfirmDeleteButton.localized, role: .destructive) {
                        viewModel.deleteGroup()
                    }
                    Button(localizables.groupConfirmCancelButton.localized, role: .cancel) {
                        viewModel.closeDeleteGroupDialog()
                    }
                },
                message: {
                    Text(localizables.groupConfirmDeleteDialogText.localized)
                }
            )
            .sheet(isPresented: .constant(data.inviteCode != nil, onSetFalse: viewModel.closeInviteCode)) {
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
