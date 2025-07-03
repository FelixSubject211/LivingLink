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

    @Namespace var itemNamespace

    var body: some View {
        let aggregate = loadableData.aggregate
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
                        ).matchedGeometryEffect(id: item.id, in: itemNamespace)
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
                            ).matchedGeometryEffect(id: item.id, in: itemNamespace)
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
        .modifier(ShoppingListScreenScaffoldModifier(
            data: data,
            viewModel: viewModel
        ))
    }
}
