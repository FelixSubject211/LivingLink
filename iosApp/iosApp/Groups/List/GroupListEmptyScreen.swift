//
//  GroupListEmptyScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupListEmptyScreen: View {
    let data: GroupListViewModel.Data
    let viewModel: GroupListViewModel
    let localizables = GroupListScreenLocalizables()

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
        .modifier(GroupListScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}
