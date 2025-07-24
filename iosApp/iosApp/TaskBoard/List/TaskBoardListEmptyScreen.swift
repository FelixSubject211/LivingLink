//
//  TaskBoardListEmptyScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct TaskBoardListEmptyScreen: View {
    let loadableData: TaskBoardListViewModel.LoadableData?
    let data: TaskBoardListViewModel.Data
    let viewModel: TaskBoardListViewModel
    let localizables = TaskBoardListScreenLocalizables()

    var body: some View {
        VStack(spacing: DesignSystem.Spacing.betweenSections) {
            Text(localizables.emptyScreenText.localized)
                .multilineTextAlignment(.center)

            DesignSystem.PrimaryButton(
                title: localizables.emptyScreenButton.localized,
                action: viewModel.showAddTask
            )
        }
        .ignoresSafeArea(.keyboard)
        .padding(DesignSystem.Padding.large)
        .modifier(TaskBoardListScreenScaffoldModifier(
            group: loadableData?.group,
            data: data,
            viewModel: viewModel
        ))
    }
}
