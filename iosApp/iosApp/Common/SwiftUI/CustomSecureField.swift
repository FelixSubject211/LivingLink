//
//  CustomSecureField.swift
//  iosApp
//
//  Created by Felix Fischer on 22.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct CustomSecureField: View {
    let label: String
    let text: String
    let onChange: (String) -> Void

    @State private var internalText: String = ""

    var body: some View {
        SecureField(label, text: $internalText)
            .onAppear {
                internalText = text
            }
            .onChange(of: text) { _, newValue in
                if newValue != internalText {
                    internalText = newValue
                }
            }
            .onChange(of: internalText) { _, newValue in
                onChange(newValue)
            }
    }
}
