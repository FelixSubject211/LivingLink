//
//  DesignSystem.swift
//  iosApp
//
//  Created by Felix Fischer on 23.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

enum DesignSystem {
    enum Colors {
        static let primary = Color("Primary")
        static let onPrimary = Color("OnPrimary")
        static let labelColor = Color("LabelColor")
    }
    
    static let bodyPadding: CGFloat = 18
    
    static let background = LinearGradient(
        gradient: Gradient(colors: [
            Color("BackgroundFirst"),
            Color("BackgroundSecond")
        ]),
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
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
