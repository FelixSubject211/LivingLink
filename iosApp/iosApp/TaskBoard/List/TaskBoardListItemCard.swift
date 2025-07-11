//
//  TaskBoardListItemCard.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct TaskBoardListItemCard: View {
    let task: TaskBoardAggregate.Task
    let onClick: () -> Void
    let localizables = TaskBoardListScreenLocalizables()

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(task.title)
                .font(.headline)
                .foregroundColor(.primary)
                .multilineTextAlignment(.leading)

            Text(task.description_.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                ? localizables.noDescriptionPlaceholder.localized
                : task.description_)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .lineLimit(1)
                .truncationMode(.tail)
                .multilineTextAlignment(.leading)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemBackground))
                .shadow(radius: 1)
        )
        .onTapGesture {
            onClick()
        }
    }
}
