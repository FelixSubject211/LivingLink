//
//  ShoppingListScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 16.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct ShoppingListScreen: View {
    let viewModel: ShoppingListViewModel
    let localizables = ShoppingListScreenLocalizables()
    
    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: ShoppingListScreenError) in
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
    
    private func content(
        loadableData: ShoppingListViewModel.LoadableData,
        data: ShoppingListViewModel.Data
    ) -> AnyView {
        VStack {
            List(loadableData.aggregate.items, id: \.id) { item in
                ShoppingListItemView(
                    item: item,
                    onCompleteItem: { viewModel.completeItem(itemId: item.id) },
                    onUnCompleteItem: { viewModel.unCompleteItem(itemId: item.id) }
                )
            }
            .scrollContentBackground(.hidden)

            Spacer()

            Button(action: viewModel.showAddItemAlert) {
                Text(localizables.addItemButton.localized)
                    .frame(maxWidth: .infinity)
            }
        }
        .ignoresSafeArea(.keyboard)
        .alertWithTextField(
            title: localizables.addItemDialogTitle.localized,
            isPresented: data.showAddItemAlert,
            placeholder: localizables.addItemDialogText.localized,
            confirmTitle: localizables.addItemDialogCreate.localized,
            cancelTitle: localizables.addItemDialogCancel.localized,
            onCancel: viewModel.closeAddItemAlert,
            onConfirm: viewModel.addItem(name:)
        )
        .eraseToAnyView()
    }

    private struct ShoppingListItemView: View {
        let item: SharedShoppingListAggregate.Item
        let onCompleteItem: () -> Void
        let onUnCompleteItem: () -> Void

        var body: some View {
            HStack {
                Text(item.name)
                    .strikethrough(item.isCompleted, color: .primary)
                    .opacity(item.isCompleted ? 0.5 : 1.0)
                
                Spacer()

                
                Toggle("", isOn: Binding(
                    get: { item.isCompleted },
                    set: { isChecked in
                        if isChecked {
                            onCompleteItem()
                        } else {
                            onUnCompleteItem()
                        }
                    }
                ))
                .toggleStyle(DesignSystem.CheckboxToggleStyle())
                .labelsHidden()
            }
        }
    }
}

fileprivate typealias ShoppingListScreenError = LoadableViewModelStateCombinedError<NetworkError, KotlinNothing, NetworkError>
