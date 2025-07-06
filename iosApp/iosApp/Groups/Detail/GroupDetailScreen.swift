//
//  GroupDetailScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 08.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupDetailScreen: View {
    let groupDetailViewModel: GroupDetailViewModel
    let shoppingListViewModel: ShoppingListListViewModel

    var body: some View {
        LoadableStatefulView(
            viewModel: groupDetailViewModel,
            buildAlert: { (error: GroupDetailScreenError) in
                error.asAlert(
                    navigator: groupDetailViewModel.navigator,
                    dismiss: groupDetailViewModel.closeError
                )
            },
            content: { loadableData, data in
                GroupDetailContentScreen(
                    loadableData: loadableData,
                    data: data,
                    viewModel: groupDetailViewModel,
                    shoppingListViewModel: shoppingListViewModel
                )
            }
        )
        .fillMaxSize()
    }
}

private typealias GroupDetailScreenError = LoadableViewModelStateCombinedError<NetworkError, GroupDetailViewModel.Error, NetworkError>
