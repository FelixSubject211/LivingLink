//
//  ShoppingListItemCard.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListItemCard: View {
    let item: ShoppingListAggregate.Item
    let onCompleteItem: () -> Void
    let onUnCompleteItem: () -> Void
    let onClick: () -> Void // <-- NEU

    var body: some View {
        HStack {
            Text(item.name)
                .strikethrough(item.isCompleted, color: .primary)
                .opacity(item.isCompleted ? 0.5 : 1.0)

            Spacer()

            Toggle("", isOn: Binding(
                get: { item.isCompleted },
                set: { isChecked in
                    if isChecked {
                        onCompleteItem()
                    } else {
                        onUnCompleteItem()
                    }
                }
            ))
            .toggleStyle(DesignSystem.CheckboxToggleStyle())
            .labelsHidden()
            .onTapGesture {}
        }
        .contentShape(Rectangle())
        .onTapGesture {
            onClick()
        }
    }
}
