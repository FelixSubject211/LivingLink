//
//  ListGroupsGroupCard.swift
//  iosApp
//
//  Created by Felix Fischer on 19.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct ListGroupsGroupCard: View {
    let group: SharedGroup
    let viewModel: ListGroupsViewModel
    let localizables = ListGroupsScreenLocalizables()

    var body: some View {
        Button(action: {
            viewModel.navigator.push(screen: LivingLinkScreen.Group(groupId: group.id))
        }, label: {
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.betweenText) {
                Text(group.name)
                    .font(.headline)
                    .foregroundColor(DesignSystem.Colors.labelColor)

                Text(localizables.groupMemberCount.localized(group.groupMemberIdsToName.count))
                    .font(.subheadline)
            }
        })
    }
}
