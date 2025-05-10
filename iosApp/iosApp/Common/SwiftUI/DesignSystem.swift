//
//  DesignSystem.swift
//  iosApp
//
//  Created by Felix Fischer on 23.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

enum DesignSystem {}

extension DesignSystem {
    enum Colors {
        static let primary = Color("primary")
        static let onPrimary = Color("onPrimary")
        static let labelColor = Color("labelColor")
    }
}

extension DesignSystem {
    enum Spacing {
        static let betweenSections: CGFloat = 24
        static let betweenElements: CGFloat = 12
        static let betweenText: CGFloat = 4
    }
    
    enum Padding {
        static let large: CGFloat = 18
        static let regular: CGFloat = 10
        static let small: CGFloat = 5
    }
}

extension DesignSystem {
    static let background = LinearGradient(
        gradient: Gradient(colors: [
            Color("backgroundFirst"),
            Color("backgroundSecond")
        ]),
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

extension DesignSystem {
    static func PrimaryButton(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .padding(Padding.regular)
                .frame(maxWidth: .infinity)
        }
        .buttonStyle(PrimaryButtonStyle())
    }
    
    static func SecondaryButton(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .padding(Padding.regular)
                .frame(maxWidth: .infinity)
        }
        .buttonStyle(SecondaryButtonStyle())
    }
}

extension DesignSystem {
    struct PrimaryButtonStyle: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Colors.primary)
                )
                .foregroundColor(Colors.onPrimary)
                .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
        }
    }

    struct SecondaryButtonStyle: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .foregroundColor(Colors.primary)
                .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
        }
    }
}

extension DesignSystem {
    struct CustomTextFieldStyle: TextFieldStyle {
        func _body(configuration: TextField<_Label>) -> some View {
            configuration
                .padding(14)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Colors.onPrimary.opacity(0.2))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Colors.onPrimary.opacity(0.3), lineWidth: 1)
                )
                .foregroundColor(Colors.labelColor)
                .autocapitalization(.none)
        }
    }
}
