//
//  TextFieldAlertModifier.swift
//  iosApp
//
//  Created by Felix Fischer on 21.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

import SwiftUI

struct TextFieldAlertModifier: ViewModifier {
    let title: String
    let message: String?
    let isPresented: Bool
    let placeholder: String
    let confirmTitle: String
    let cancelTitle: String
    let onCancel: () -> Void
    let onConfirm: (String) -> Void

    @State private var inputText: String = ""

    func body(content: Content) -> some View {
        content
            .alert(title, isPresented: .constant(isPresented, onSetFalse: onCancel)) {
                TextField(placeholder, text: $inputText)

                Button(cancelTitle, role: .cancel) {
                    inputText = ""
                    onCancel()
                }

                Button(confirmTitle) {
                    let trimmed = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !trimmed.isEmpty {
                        onConfirm(trimmed)
                        inputText = ""
                    }
                }
            } message: {
                if let message = message {
                    Text(message)
                }
            }
    }
}
