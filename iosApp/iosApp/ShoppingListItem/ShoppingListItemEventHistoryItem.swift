//
//  ShoppingListItemEventHistoryItem.swift
//  iosApp
//
//  Created by Felix Fischer on 28.06.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ShoppingListItemEventHistoryItem: View {
    let event: SharedEventSourcingEvent
    let viewModel: ShoppingListItemViewModel
    let localizables = ShoppingListItemScreenLocalizables()

    @ObservedObject var userName: FlowObservable<String>

    init(event: SharedEventSourcingEvent, viewModel: ShoppingListItemViewModel) {
        self.event = event
        self.viewModel = viewModel
        userName = viewModel.resolveUserName(userId: event.userId).asObservableObject()
    }

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: iconName())
                .resizable()
                .frame(width: 28, height: 28)
                .foregroundColor(.accentColor)

            VStack(alignment: .leading, spacing: 4) {
                Text(descriptionText())
                    .font(.body)

                Text(event.createdAt.formatWith(style: DateFormatStyle.dateTime))
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 10)
    }

    private func descriptionText() -> String {
        let displayName = userName.value ?? localizables.eventPerformedByUnknown.localized
        switch event.payload {
        case is SharedShoppingListEvent.ItemAdded:
            return localizables.eventAdded.localized(displayName)
        case is SharedShoppingListEvent.ItemCompleted:
            return localizables.eventCompleted.localized(displayName)
        case is SharedShoppingListEvent.ItemUncompleted:
            return localizables.eventUncompleted.localized(displayName)
        default:
            return localizables.eventUnknown.localized(displayName)
        }
    }

    private func iconName() -> String {
        switch event.payload {
        case is SharedShoppingListEvent.ItemAdded:
            return "plus"
        case is SharedShoppingListEvent.ItemCompleted:
            return "checkmark"
        case is SharedShoppingListEvent.ItemUncompleted:
            return "xmark"
        default:
            return "questionmark"
        }
    }
}
