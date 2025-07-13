//
//  GroupRemoveMemberConfirmationAlert.swift
//  iosApp
//
//  Created by Felix Fischer on 13.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupRemoveMemberConfirmationAlert: ViewModifier {
    let isPresented: Bool
    let onConfirm: () -> Void
    let onCancel: () -> Void
    let localizables = GroupsCommonLocalizables()

    func body(content: Content) -> some View {
        content
            .alert(
                localizables.groupConfirmRemoveUserDialogTitle.localized,
                isPresented: .constant(isPresented),
                actions: {
                    Button(localizables.groupConfirmRemoveUserButton.localized, role: .destructive, action: onConfirm)
                    Button(localizables.groupConfirmCancelButton.localized, role: .cancel, action: onCancel)
                },
                message: {
                    Text(localizables.groupConfirmRemoveUserDialogText.localized)
                }
            )
    }
}
