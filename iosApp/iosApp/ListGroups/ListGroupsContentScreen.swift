//
//  ListGroupsContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ListGroupsContentScreen: View {
    let loadableData: ListGroupsViewModel.LoadableData
    let data: ListGroupsViewModel.Data
    let viewModel: ListGroupsViewModel
    let localizables = ListGroupsScreenLocalizables()

    var body: some View {
        VStack {
            List {
                ForEach(loadableData.groups) { group in
                    ListGroupsGroupCard(
                        group: group,
                        viewModel: viewModel
                    )
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
        .modifier(ListGroupsScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}

extension SharedGroup: @retroactive Identifiable {}
