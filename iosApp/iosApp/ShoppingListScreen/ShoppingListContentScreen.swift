//
//  ShoppingListContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListContentScreen: View {
    let loadableData: ShoppingListViewModel.LoadableData
    let data: ShoppingListViewModel.Data
    let viewModel: ShoppingListViewModel
    let localizables = ShoppingListScreenLocalizables()

    var body: some View {
        VStack {
            List(loadableData.aggregate.asReversedList(), id: \.id) { item in
                ShoppingListItemCard(
                    item: item,
                    onCompleteItem: { viewModel.completeItem(itemId: item.id) },
                    onUnCompleteItem: { viewModel.unCompleteItem(itemId: item.id) },
                    onClick: {
                        viewModel.navigator.push(screen: LivingLinkScreen.ShoppingListItem(
                            groupId: viewModel.groupId,
                            itemId: item.id
                        ))
                    }
                )
            }
            .scrollContentBackground(.hidden)

            Spacer()

            Button(action: viewModel.showAddItem) {
                Text(localizables.addItemButton.localized)
                    .frame(maxWidth: .infinity)
            }
            .padding()
        }
        .ignoresSafeArea(.keyboard)
        .modifier(ShoppingListScreenScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}
