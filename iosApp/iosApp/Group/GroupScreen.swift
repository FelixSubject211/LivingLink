//
//  GroupScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 08.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupScreen: View {
    let groupViewModel: GroupViewModel
    let shoppingListViewModel: ShoppingListViewModel

    var body: some View {
        LoadableStatefulView(
            viewModel: groupViewModel,
            buildAlert: { (error: GroupScreenError) in
                error.asAlert(
                    navigator: groupViewModel.navigator,
                    dismiss: groupViewModel.closeError
                )
            },
            content: { loadableData, data in
                GroupContentScreen(
                    loadableData: loadableData,
                    data: data,
                    groupViewModel: groupViewModel,
                    shoppingListViewModel: shoppingListViewModel
                )
            }
        )
        .fillMaxSize()
    }
}

private typealias GroupScreenError = LoadableViewModelStateCombinedError<NetworkError, GroupViewModel.Error, NetworkError>
