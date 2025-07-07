//
//  TaskBoardListContentScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct TaskBoardListContentScreen: View {
    let loadableData: TaskBoardListViewModel.LoadableData
    let data: TaskBoardListViewModel.Data
    let viewModel: TaskBoardListViewModel
    let localizables = TaskBoardListScreenLocalizables()

    var body: some View {
        let tasks = loadableData.aggregate.tasksReversed()

        VStack(spacing: 0) {
            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(tasks, id: \.id) { task in
                        TaskBoardListItemCard(
                            task: task,
                            onClick: {}
                        )
                        .padding(.horizontal, 12)
                    }
                }
                .padding(.top, 12)
            }

            Button(action: viewModel.showAddTask) {
                Text(localizables.addTaskButton.localized)
                    .frame(maxWidth: .infinity)
            }
            .padding()
        }
        .ignoresSafeArea(.keyboard)
        .modifier(TaskBoardListScreenScaffoldModifier(data: data, viewModel: viewModel))
    }
}
