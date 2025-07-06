//
//  GroupListContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupListContentScreen: View {
    let loadableData: GroupListViewModel.LoadableData
    let data: GroupListViewModel.Data
    let viewModel: GroupListViewModel
    let localizables = GroupListScreenLocalizables()

    var body: some View {
        VStack {
            List {
                ForEach(loadableData.groups) { group in
                    GroupListGroupCard(
                        group: group,
                        viewModel: viewModel
                    )
                }
                .onDelete { indexSet in
                    if let index = indexSet.first {
                        let group = loadableData.groups[index]
                        viewModel.showDeleteGroupDialog(groupId: group.id)
                    }
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
        .modifier(GroupListScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}

extension SharedGroup: @retroactive Identifiable {}
