//
//  ShoppingListListContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListListContentScreen: View {
    let loadableData: ShoppingListListViewModel.LoadableData
    let data: ShoppingListListViewModel.Data
    let viewModel: ShoppingListListViewModel
    let localizables = ShoppingListListScreenLocalizables()

    @Namespace var itemNamespace

    var body: some View {
        let aggregate = loadableData.shoppingListAggregate
        let openItems = aggregate.openItemsReversed()
        let completedItems = aggregate.completedItemsReversed()
        let visibleCompletedItems: [ShoppingListAggregate.Item] = {
            if let limit = data.completedItemsLimit?.int32Value {
                return Array(completedItems.prefix(Int(limit)))
            } else {
                return []
            }
        }()

        VStack(spacing: 0) {
            List {
                Section {
                    ForEach(openItems, id: \.id) { item in
                        ShoppingListListItemCard(
                            item: item,
                            onCompleteItem: { viewModel.completeItem(itemId: item.id) },
                            onUnCompleteItem: { viewModel.unCompleteItem(itemId: item.id) },
                            onClick: {
                                viewModel.navigator.push(screen: LivingLinkScreen.ShoppingListDetail(
                                    groupId: viewModel.groupId,
                                    itemId: item.id
                                ))
                            }
                        ).matchedGeometryEffect(id: item.id, in: itemNamespace)
                    }
                    .onDelete { indexSet in
                        if let index = indexSet.first {
                            let item = openItems[index]
                            viewModel.deleteItem(itemId: item.id)
                        }
                    }
                }

                if !completedItems.isEmpty {
                    Section {
                        Button(action: { viewModel.toggleShowCompletedItems() }) {
                            Text(
                                data.showCompletedItems
                                    ? localizables.hideCompletedItemsButton.localized
                                    : localizables.showCompletedItemsButton.localized
                            )
                            .frame(maxWidth: .infinity, alignment: .center)
                        }
                    }
                }

                if data.showCompletedItems && !visibleCompletedItems.isEmpty {
                    Section {
                        ForEach(visibleCompletedItems, id: \.id) { item in
                            ShoppingListListItemCard(
                                item: item,
                                onCompleteItem: { viewModel.completeItem(itemId: item.id) },
                                onUnCompleteItem: { viewModel.unCompleteItem(itemId: item.id) },
                                onClick: {
                                    viewModel.navigator.push(screen: LivingLinkScreen.ShoppingListDetail(
                                        groupId: viewModel.groupId,
                                        itemId: item.id
                                    ))
                                }
                            ).matchedGeometryEffect(id: item.id, in: itemNamespace)
                        }
                        .onDelete { indexSet in
                            if let index = indexSet.first {
                                let item = completedItems[index]
                                viewModel.deleteItem(itemId: item.id)
                            }
                        }

                        if visibleCompletedItems.count < completedItems.count {
                            Button(action: { viewModel.showMoreCompletedItems() }) {
                                Text(localizables.showMoreCompletedItemsButton.localized)
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .padding(.vertical, 4)
                            }
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)

            Button(action: viewModel.showAddItem) {
                Text(localizables.addItemButton.localized)
                    .frame(maxWidth: .infinity)
            }
            .padding()
        }
        .ignoresSafeArea(.keyboard)
        .modifier(ShoppingListListScreenScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}
