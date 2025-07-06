//
//  ShoppingListItemContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 28.06.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListItemContentScreen: View {
    let loadableData: ShoppingListDetailViewModel.LoadableData
    let data: KotlinUnit
    let viewModel: ShoppingListDetailViewModel

    var body: some View {
        VStack {
            List(loadableData.aggregate.history()) { event in
                ShoppingListItemEventHistoryItem(
                    event: event,
                    viewModel: viewModel
                )
            }
            .scrollContentBackground(.hidden)
            .listStyle(.plain)

            Spacer()
        }
        .ignoresSafeArea(.keyboard)
        .navigationTitle(loadableData.aggregate.itemName ?? "")
    }
}

extension SharedEventSourcingEvent: @retroactive Identifiable {}
