//
//  ShoppingListScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 16.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListScreen: View {
    let viewModel: ShoppingListViewModel

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: ShoppingListScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            emptyContent: { data in
                ShoppingListEmptyScreen(data: data, viewModel: viewModel)
            },
            content: { loadbaleData, data in
                ShoppingListContentScreen(
                    loadableData: loadbaleData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .fillMaxSize()
    }
}

private typealias ShoppingListScreenError = LoadableViewModelStateCombinedError<NetworkError, KotlinNothing, NetworkError>
