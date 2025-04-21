//
//   LivingLinkErrorExtension.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

extension LivingLinkError {
    func asBasicAlert() -> Alert {
        if let message = message() {
            return Alert(
                title: Text(title()),
                message: Text(message),
                dismissButton: .default(Text(CommonLocalizables().ok.localized))
            )
        } else {
            return Alert(
                title: Text(title()),
                dismissButton: .default(Text(CommonLocalizables().ok.localized))
            )
        }
    }
    
    func asBasicErrorView() -> AnyView {
        VStack {
            Text(self.title()).bold()
            
            if let message = self.message() {
                Text(message)
            }
        }.eraseToAnyView()
    }
}
