//
//  CustomTextEditor.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct CustomTextEditor: View {
    let label: String
    let text: String
    let onChange: (String) -> Void

    @State private var internalText: String = ""
    @FocusState private var isFocused: Bool

    init(label: String, text: String, onChange: @escaping (String) -> Void) {
        self.label = label
        self.text = text
        self.onChange = onChange
        UITextView.appearance().backgroundColor = .clear
    }

    var body: some View {
        ZStack(alignment: .topLeading) {
            if internalText.isEmpty && !isFocused {
                Text(label)
                    .foregroundColor(.secondary)
                    .padding(14)
            }

            TextEditor(text: $internalText)
                .scrollContentBackground(.hidden) // iOS 16+
                .padding(14)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(DesignSystem.Colors.onPrimary.opacity(0.2))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(DesignSystem.Colors.onPrimary.opacity(0.3), lineWidth: 1)
                )
                .foregroundColor(DesignSystem.Colors.labelColor)
                .font(.body)
                .focused($isFocused)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.none)
                .onAppear { internalText = text }
                .onChange(of: text) { _, new in if new != internalText { internalText = new } }
                .onChange(of: internalText) { _, new in onChange(new) }
        }
        .frame(minHeight: 100)
    }
}
