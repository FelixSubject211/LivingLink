//
//  CustomTextField.swift
//  iosApp
//
//  Created by Felix Fischer on 22.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct CustomTextField: View {
    let label: String
    let text: String
    let onChange: (String) -> Void

    @State private var internalText: String = ""

    var body: some View {
        TextField(label, text: $internalText)
            .onAppear {
                internalText = text
            }
            .onChange(of: text) { newValue in
                if newValue != internalText {
                    internalText = newValue
                }
            }
            .onChange(of: internalText) { newValue in
                onChange(newValue)
            }
    }
}
