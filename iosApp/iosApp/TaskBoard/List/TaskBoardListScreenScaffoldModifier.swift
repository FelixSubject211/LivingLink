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
    let data: TaskBoardListViewModel.Data
    let viewModel: TaskBoardListViewModel
    let localizables = TaskBoardListScreenLocalizables()

    func body(content: Content) -> some View {
        content
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }.sheet(isPresented: .constant(data.showAddTask, onSetFalse: viewModel.closeAddTask)) {
                ModalBottomSheet(
                    title: localizables.addTaskSheetTitle.localized,
                    onDismiss: viewModel.closeAddTask,
                    onConfirm: viewModel.addTask,
                    confirmButtonEnabled: viewModel.addTaskConfirmButtonEnabled()
                ) {
                    VStack(spacing: DesignSystem.Spacing.betweenElements) {
                        CustomTextField(
                            label: localizables.addTaskSheetTitleLabel.localized,
                            text: data.addTaskTitle,
                            onChange: viewModel.updateAddTaskTitle(title:)
                        ).textFieldStyle(DesignSystem.CustomTextFieldStyle())

                        CustomTextEditor(
                            label: localizables.addTaskSheetDescriptionLabel.localized,
                            text: data.addTaskDescription,
                            onChange: viewModel.updateAddTaskDescription(description:)
                        )
                    }
                }
            }
    }
}
