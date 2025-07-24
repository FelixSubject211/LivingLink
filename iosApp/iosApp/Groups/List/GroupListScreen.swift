//
//  GroupListScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 21.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupListScreen: View {
    let viewModel: GroupListViewModel
    let localizables = GroupsListScreenLocalizables()

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: ListGroupsScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            emptyContent: { _, data in
                GroupListEmptyScreen(
                    data: data,
                    viewModel: viewModel
                )
            },
            content: { loadbaleData, data in
                GroupListContentScreen(
                    loadableData: loadbaleData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .navigationTitle(localizables.navigationTitle.localized)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.navigator.push(screen: LivingLinkScreen.Settings())
                }) {
                    Image(systemName: "gearshape")
                        .imageScale(.large)
                        .padding()
                        .accessibilityLabel(localizables.showSettingsIconContentDescription.localized)
                }
            }
        }
    }
}

private typealias ListGroupsScreenError = LoadableViewModelStateCombinedError<NetworkError, GroupListViewModel.Error, NetworkError>
