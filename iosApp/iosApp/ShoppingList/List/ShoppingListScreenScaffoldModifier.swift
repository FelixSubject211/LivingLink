//
//  ShoppingListScreenScaffoldModifier.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListScreenScaffoldModifier: ViewModifier {
    let data: ShoppingListListViewModel.Data
    let viewModel: ShoppingListListViewModel
    let localizables = ShoppingListListScreenLocalizables()

    func body(content: Content) -> some View {
        content
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }
            .alertWithTextField(
                title: localizables.addItemDialogTitle.localized,
                isPresented: data.showAddItem,
                placeholder: localizables.addItemDialogText.localized,
                confirmTitle: localizables.addItemDialogCreate.localized,
                cancelTitle: localizables.addItemDialogCancel.localized,
                onCancel: viewModel.closeAddItem,
                onConfirm: viewModel.addItem(name:)
            )
    }
}
