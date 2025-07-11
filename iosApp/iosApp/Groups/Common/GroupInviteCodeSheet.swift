//
//  GroupInviteCodeSheet.swift
//  iosApp
//
//  Created by Felix Fischer on 06.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupInviteCodeSheet: View {
    let inviteCode: String
    let localizables = GroupsCommonLocalizables()

    var body: some View {
        VStack(spacing: DesignSystem.Spacing.betweenElements) {
            Text(localizables.groupInviteDialogTitle.localized)
                .font(.headline)

            Text(localizables.groupInviteDialogText.localized)
                .font(.body)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            Text(inviteCode)
                .font(.system(.title, design: .monospaced))
                .fontWeight(.bold)
                .padding(.vertical, 8)
                .padding(.horizontal, 24)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.secondary.opacity(0.1))
                )
        }
        .padding()
        .presentationDetents([.height(180)])
        .presentationDragIndicator(.visible)
    }
}
