//
//  ListGroupsEmptyScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ListGroupsEmptyScreen: View {
    let data: ListGroupsViewModel.Data
    let viewModel: ListGroupsViewModel
    let localizables = ListGroupsScreenLocalizables()

    var body: some View {
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
        .modifier(ListGroupsScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}
