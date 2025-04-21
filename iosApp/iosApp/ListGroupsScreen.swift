//
//  ListGroups.swift
//  iosApp
//
//  Created by Felix Fischer on 21.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct ListGroupsScreen: View {
    let viewModel: ListGroupsViewModel
    let localizables = ListGroupsScreenLocalizables()
    
    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: ListGroupsScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            emptyContent: emptyContent,
            content: content
        )
        .navigationTitle(localizables.navigationTitle.localized)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.navigator.push(screen: LivingLinkScreen.Settings())
                }) {
                    Image(systemName: "gearshape")
                        .imageScale(.large)
                        .padding()
                        .accessibilityLabel(localizables.showSettingsIconContentDescription.localized)
                }
            }
        }
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
    }
    
    private func content(loadableData: ListGroupsViewModel.LoadableData, data: ListGroupsViewModel.Data) -> AnyView {
        VStack {
            List {
                ForEach(loadableData.groups) { group in
                    groupCard(group)
                }
            }
            .listStyle(.sidebar)
            .scrollContentBackground(.hidden)
            
            VStack(spacing: DesignSystem.Spacing.betweenElements) {
                DesignSystem.PrimaryButton(
                    title: localizables.joinGroupButtonTitle.localized,
                    action: viewModel.showJoinGroupDialog
                )
                
                DesignSystem.SecondaryButton(
                    title: localizables.createGroupButtonTitle.localized,
                    action: viewModel.showAddGroupDialog
                )
            }
            .padding(.horizontal, DesignSystem.Padding.large)
        }
        .ignoresSafeArea(.keyboard)
        .withGroupScreenBackgroundAndAlerts(data: data, viewModel: viewModel)
        .eraseToAnyView()
    }
    
    private func emptyContent(data: ListGroupsViewModel.Data) -> AnyView {
        VStack(spacing: DesignSystem.Spacing.betweenSections) {
            DesignSystem.PrimaryButton(
                title: localizables.joinGroupButtonTitle.localized,
                action: viewModel.showJoinGroupDialog
            )
            
            DesignSystem.SecondaryButton(
                title: localizables.createGroupButtonTitle.localized,
                action: viewModel.showAddGroupDialog
            )
        }
        .ignoresSafeArea(.keyboard)
        .padding(DesignSystem.Padding.large)
        .withGroupScreenBackgroundAndAlerts(data: data, viewModel: viewModel)
        .eraseToAnyView()
    }
    
    private func groupCard(_ group: SharedGroup) -> some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.betweenText) {
            Text(group.name)
                .font(.headline)
                .foregroundColor(DesignSystem.Colors.labelColor)

            Text(localizables.groupMemberCount.localized(group.groupMemberIdsToName.count))
                .font(.subheadline)
        }
    }
}

private extension View {
    func withGroupScreenBackgroundAndAlerts(
        data: ListGroupsViewModel.Data,
        viewModel: ListGroupsViewModel
    ) -> some View {
        let localizables = ListGroupsScreenLocalizables()
        
        return self
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }
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

extension SharedGroup: @retroactive Identifiable {}

fileprivate typealias ListGroupsScreenError = LoadableViewModelStateCombinedError<NetworkError, ListGroupsViewModel.Error, NetworkError>
