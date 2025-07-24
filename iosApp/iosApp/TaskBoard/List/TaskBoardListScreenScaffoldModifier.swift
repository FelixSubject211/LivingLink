//
//  TaskBoardListScreenScaffoldModifier.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct TaskBoardListScreenScaffoldModifier: ViewModifier {
    let group: SharedGroup?
    let data: TaskBoardListViewModel.Data
    let viewModel: TaskBoardListViewModel

    func body(content: Content) -> some View {
        content
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }.sheet(isPresented: .constant(data.showAddTask, onSetFalse: viewModel.closeAddTask)) {
                TaskBoardListAddTaskSheet(group: group, data: data, viewModel: viewModel)
            }
    }
}
