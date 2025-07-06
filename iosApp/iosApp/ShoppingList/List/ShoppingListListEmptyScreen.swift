//
//  ShoppingListListEmptyScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListListEmptyScreen: View {
    let data: ShoppingListListViewModel.Data
    let viewModel: ShoppingListListViewModel
    let localizables = ShoppingListListScreenLocalizables()

    var body: some View {
        VStack(spacing: DesignSystem.Spacing.betweenSections) {
            Text(localizables.emptyScreenText.localized)
                .multilineTextAlignment(.center)
            DesignSystem.PrimaryButton(
                title: localizables.emptyScreenButton.localized,
                action: viewModel.showAddItem
            )
        }
        .ignoresSafeArea(.keyboard)
        .padding(DesignSystem.Padding.large)
        .modifier(ShoppingListListScreenScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}
