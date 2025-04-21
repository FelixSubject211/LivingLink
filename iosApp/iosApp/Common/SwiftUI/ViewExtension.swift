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
        self
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: alignment)
    }
    
    func eraseToAnyView() -> AnyView {
        AnyView(self)
    }
    
    func alertWithTextField(
        title: String,
        message: String? = nil,
        isPresented: Bool,
        placeholder: String,
        confirmTitle: String,
        cancelTitle: String,
        onCancel: @escaping () -> Void,
        onConfirm: @escaping (String) -> Void
    ) -> some View {
        self.modifier(TextFieldAlertModifier(
            title: title,
            message: message,
            isPresented: isPresented,
            placeholder: placeholder,
            confirmTitle: confirmTitle,
            cancelTitle: cancelTitle,
            onCancel: onCancel,
            onConfirm: onConfirm
        ))
    }
}
