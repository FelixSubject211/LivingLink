//
//  ShoppingListListScreenScaffoldModifier.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListListScreenScaffoldModifier: ViewModifier {
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
                message: nil,
                isPresented: data.showAddItem,
                text: data.addItemName,
                onTextChange: viewModel.updateAddItemName(addItemName:),
                placeholder: localizables.addItemDialogText.localized,
                confirmButtonTitle: localizables.addItemDialogCreate.localized,
                cancelButtonTitle: localizables.addItemDialogCancel.localized,
                isConfirmButtonEnabled: viewModel.addItemConfirmButtonEnabled(),
                onCancel: viewModel.closeAddItem,
                onConfirm: viewModel.addItem
            )
    }
}
