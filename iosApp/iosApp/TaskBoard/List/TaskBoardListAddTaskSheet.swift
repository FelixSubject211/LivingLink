//
//  TaskBoardListAddTaskSheet.swift
//  iosApp
//
//  Created by Felix Fischer on 23.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct TaskBoardListAddTaskSheet: View {
    let group: SharedGroup?
    let data: TaskBoardListViewModel.Data
    let viewModel: TaskBoardListViewModel
    let localizables = TaskBoardListScreenLocalizables()
    @FocusState private var focusedField: Field?

    var body: some View {
        ModalBottomSheet(
            title: localizables.addTaskSheetTitle.localized,
            onDismiss: viewModel.closeAddTask,
            onConfirm: viewModel.addTask,
            confirmButtonEnabled: viewModel.addTaskConfirmButtonEnabled()
        ) {
            VStack(spacing: DesignSystem.Spacing.betweenElements) {
                Section {
                    CustomTextField(
                        label: localizables.addTaskSheetTitleLabel.localized,
                        text: data.addTaskTitle,
                        onChange: viewModel.updateAddTaskTitle(title:)
                    )
                    .textFieldStyle(DesignSystem.CustomTextFieldStyle())
                    .focused($focusedField, equals: .title)
                    .submitLabel(.next)
                    .onSubmit {
                        focusedField = .description
                    }

                    CustomTextEditor(
                        label: localizables.addTaskSheetDescriptionLabel.localized,
                        text: data.addTaskDescription,
                        onChange: viewModel.updateAddTaskDescription(description:)
                    )
                    .focused($focusedField, equals: .description)
                }

                CustomMultiPicker(
                    title: localizables.addTaskSheetMembersLabel.localized,
                    options: group?.groupMembersSortedByRoleAndName.map { $0.id } ?? [],
                    selected: data.selectedMemberIds,
                    valueToString: { id in
                        group?.groupMemberIdsToName[id] ?? ""
                    },
                    label: {
                        Text(localizables.addTaskSheetMembersLabel.localized)
                    },
                    onSelectionChanged: { selected in
                        viewModel.toggleMember(userId: selected)
                    }
                )
                .padding(.vertical, DesignSystem.Padding.small)
            }
            .onAppear { focusedField = .title }
        }
    }

    private enum Field {
        case title
        case description
    }
}
