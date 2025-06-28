//
//  InvisibleAlertHost.swift
//  iosApp
//
//  Created by Felix Fischer on 28.06.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct InvisibleAlertHost<Error: LivingLinkError>: View {
    @Binding var error: Error?
    let buildAlert: (Error) -> Alert
    let onDismiss: () -> Void

    var body: some View {
        Color.clear
            .frame(width: 1, height: 1)
            .allowsHitTesting(false)
            .accessibilityHidden(true)
            .alert(isPresented: Binding(
                get: { error != nil },
                set: { if !$0 { onDismiss() } }
            )) {
                buildAlert(error!)
            }
    }
}
