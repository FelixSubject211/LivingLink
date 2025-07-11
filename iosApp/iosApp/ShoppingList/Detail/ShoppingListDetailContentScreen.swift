//
//  ShoppingListDetailContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 28.06.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListDetailContentScreen: View {
    let loadableData: ShoppingListDetailViewModel.LoadableData
    let data: ShoppingListDetailViewModel.Data
    let viewModel: ShoppingListDetailViewModel

    let localizables = ShoppingListDetailScreenLocalizables()

    var body: some View {
        VStack {
            List(loadableData.historyItemAggregate.history()) { event in
                ShoppingListDetailEventHistoryItem(
                    event: event,
                    viewModel: viewModel,
                    group: loadableData.group
                )
            }
            .scrollContentBackground(.hidden)
            .listStyle(.plain)

            Spacer()
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    Button(localizables.menuDeleteItem.localized) {
                        viewModel.deleteItem()
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .accessibilityLabel(localizables.moreOptionsContentDescription.localized)
                }
            }
        }
        .ignoresSafeArea(.keyboard)
        .navigationTitle(loadableData.historyItemAggregate.itemName ?? "")
    }
}

extension SharedEventSourcingEvent: @retroactive Identifiable {}
