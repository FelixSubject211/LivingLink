//
//  BindingExtension.swift
//  iosApp
//
//  Created by Felix Fischer on 27.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

extension Binding where Value == Bool {
    static func constant(_ value: Bool, onSetFalse: @escaping () -> Void) -> Binding<Bool> {
        Binding(
            get: { value },
            set: { newValue in
                if !newValue {
                    onSetFalse()
                }
            }
        )
    }
}
