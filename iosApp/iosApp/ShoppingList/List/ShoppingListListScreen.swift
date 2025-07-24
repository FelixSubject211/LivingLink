//
//  ShoppingListListScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 16.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListListScreen: View {
    let viewModel: ShoppingListListViewModel
    let localizables = ShoppingListListScreenLocalizables()

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: ShoppingListListScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            emptyContent: { _, data in
                ShoppingListListEmptyScreen(data: data, viewModel: viewModel)
            },
            content: { loadbaleData, data in
                ShoppingListListContentScreen(
                    loadableData: loadbaleData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .fillMaxSize()
    }
}

private typealias ShoppingListListScreenError = LoadableViewModelStateCombinedError<LivingLinkError, KotlinNothing, NetworkError>
