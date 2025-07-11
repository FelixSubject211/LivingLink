//
//  ViewExtension.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

extension View {
    func fillMaxSize(
        alignment: Alignment = .center
    ) -> some View {
        frame(maxWidth: .infinity, maxHeight: .infinity, alignment: alignment)
    }

    func eraseToAnyView() -> AnyView {
        AnyView(self)
    }

    func alertWithTextField(
        title: String,
        message: String? = nil,
        isPresented: Bool,
        text: String,
        onTextChange: @escaping (String) -> Void,
        placeholder: String,
        confirmButtonTitle: String,
        cancelButtonTitle: String,
        isConfirmButtonEnabled: Bool,
        onCancel: @escaping () -> Void,
        onConfirm: @escaping () -> Void
    ) -> some View {
        modifier(TextFieldAlertModifier(
            title: title,
            message: message,
            isPresented: isPresented,
            text: text,
            onTextChange: onTextChange,
            placeholder: placeholder,
            confirmButtonTitle: confirmButtonTitle,
            cancelButtonTitle: cancelButtonTitle,
            isConfirmButtonEnabled: isConfirmButtonEnabled,
            onCancel: onCancel,
            onConfirm: onConfirm
        ))
    }
}
