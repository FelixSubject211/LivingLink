//
//  GroupLeaveConfirmationAlert.swift
//  iosApp
//
//  Created by Codex on 11.07.25.
//

import ComposeApp
import SwiftUI

struct GroupLeaveConfirmationAlert: ViewModifier {
    let isPresented: Bool
    let onConfirm: () -> Void
    let onCancel: () -> Void
    let localizables = GroupsCommonLocalizables()

    func body(content: Content) -> some View {
        content
            .alert(
                localizables.groupConfirmLeaveDialogTitle.localized,
                isPresented: .constant(isPresented),
                actions: {
                    Button(localizables.groupConfirmLeaveButton.localized, role: .destructive, action: onConfirm)
                    Button(localizables.groupConfirmCancelButton.localized, role: .cancel, action: onCancel)
                },
                message: {
                    Text(localizables.groupConfirmLeaveDialogText.localized)
                }
            )
    }
}
