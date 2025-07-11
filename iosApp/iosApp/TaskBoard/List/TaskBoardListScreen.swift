//
//  TaskBoardListScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct TaskBoardListScreen: View {
    let viewModel: TaskBoardListViewModel
    let localizables = TaskBoardListScreenLocalizables()

    var body: some View {
        LoadableStatefulView(
            viewModel: viewModel,
            buildAlert: { (error: TaskBoardListScreenError) in
                error.asAlert(
                    navigator: viewModel.navigator,
                    dismiss: viewModel.closeError
                )
            },
            emptyContent: { data in
                TaskBoardListEmptyScreen(data: data, viewModel: viewModel)
            },
            content: { loadableData, data in
                TaskBoardListContentScreen(
                    loadableData: loadableData,
                    data: data,
                    viewModel: viewModel
                )
            }
        )
        .fillMaxSize()
        .navigationTitle(localizables.navigationTitle.localized)
    }
}

private typealias TaskBoardListScreenError = LoadableViewModelStateCombinedError<LivingLinkError, KotlinNothing, NetworkError>
