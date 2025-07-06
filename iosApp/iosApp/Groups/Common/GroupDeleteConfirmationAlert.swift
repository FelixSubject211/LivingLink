//
//  GroupDeleteConfirmationAlert.swift
//  iosApp
//
//  Created by Felix Fischer on 06.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupDeleteConfirmationAlert: ViewModifier {
    let isPresented: Bool
    let onConfirm: () -> Void
    let onCancel: () -> Void
    let localizables = GroupCommonLocalizables()

    func body(content: Content) -> some View {
        content
            .alert(
                localizables.groupConfirmDeleteDialogTitle.localized,
                isPresented: .constant(isPresented),
                actions: {
                    Button(localizables.groupConfirmDeleteButton.localized, role: .destructive, action: onConfirm)
                    Button(localizables.groupConfirmCancelButton.localized, role: .cancel, action: onCancel)
                },
                message: {
                    Text(localizables.groupConfirmDeleteDialogText.localized)
                }
            )
    }
}
