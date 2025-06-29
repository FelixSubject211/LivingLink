//
//  ShoppingListItemScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 28.06.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListItemScreen: View {
    let viewModel: ShoppingListItemViewModel

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: ShoppingListItemScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            content: { loadbaleData, data in
                ShoppingListItemContentScreen(
                    loadableData: loadbaleData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .fillMaxSize()
    }
}

private typealias ShoppingListItemScreenError = LoadableViewModelStateCombinedError<LivingLinkError, KotlinNothing, NetworkError>
