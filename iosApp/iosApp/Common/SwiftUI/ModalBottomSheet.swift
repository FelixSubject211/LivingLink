//
//  ModalBottomSheet.swift
//  iosApp
//
//  Created by Felix Fischer on 07.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct ModalBottomSheet<Content: View>: View {
    let title: String
    let onDismiss: () -> Void
    let onConfirm: () -> Void
    let confirmButtonEnabled: Bool
    let content: Content

    init(
        title: String,
        onDismiss: @escaping () -> Void,
        onConfirm: @escaping () -> Void,
        confirmButtonEnabled: Bool = true,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.onDismiss = onDismiss
        self.onConfirm = onConfirm
        self.confirmButtonEnabled = confirmButtonEnabled
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .imageScale(.large)
                        .padding(12)
                }

                Spacer()

                Text(title)
                    .font(.headline)
                    .lineLimit(1)
                    .truncationMode(.tail)

                Spacer()

                Button(action: onConfirm) {
                    Image(systemName: "checkmark")
                        .imageScale(.large)
                        .padding(12)
                }
                .disabled(!confirmButtonEnabled)
                .opacity(confirmButtonEnabled ? 1.0 : 0.3)
            }
            .padding(.horizontal)
            .padding(.top)
            .background(Color.clear)

            Divider()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    content
                }
                .padding(24)
            }
        }
        .background(DesignSystem.background.ignoresSafeArea())
    }
}
