//
//  TextFieldAlertModifier.swift
//  iosApp
//
//  Created by Felix Fischer on 21.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct TextFieldAlertModifier: ViewModifier {
    let title: String
    let message: String?

    let isPresented: Bool

    let text: String
    let onTextChange: (String) -> Void
    let placeholder: String

    let confirmButtonTitle: String
    let cancelButtonTitle: String
    let isConfirmButtonEnabled: Bool

    let onCancel: () -> Void
    let onConfirm: () -> Void

    @State private var confirmTapped = false

    func body(content: Content) -> some View {
        content
            .alert(title, isPresented: .constant(isPresented, onSetFalse: {
                if !confirmTapped {
                    onCancel()
                }
                confirmTapped = false
            })) {
                TextField(placeholder, text: .init(get: { text }, set: onTextChange))

                Button(cancelButtonTitle, role: .cancel) {
                    onCancel()
                }

                Button(confirmButtonTitle) {
                    confirmTapped = true
                    onConfirm()
                }
                .disabled(!isConfirmButtonEnabled)

            } message: {
                if let message = message {
                    Text(message)
                }
            }
    }
}
