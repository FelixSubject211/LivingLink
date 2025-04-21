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
    
    var unwrapped: LivingLinkError {
        switch self {
        case let combined as ViewModelStateCombinedError<AnyObject, AnyObject>:
            return combined.value
        case let loadableCombined as LoadableViewModelStateCombinedError<AnyObject, AnyObject, AnyObject>:
            return loadableCombined.value
        default:
            return self
        }
    }
    
    func asAlert(
        navigator: Navigator,
        dismiss: @escaping () -> Void
    ) -> Alert {
        switch unwrapped {
        case is NetworkError.Unauthorized:
            return Alert(
                title: Text(title()),
                message: nil,
                primaryButton: .default(Text(CommonLocalizables().ok.localized), action: dismiss),
                secondaryButton: .default(
                    Text(CommonLocalizables().navigateToLoginButtonTitle.localized),
                    action: {
                        dismiss()
                        navigator.push(screen: LivingLinkScreen.Login())
                    }
                )
            )
            
        default:
            if let message = message() {
                return Alert(
                    title: Text(title()),
                    message: Text(message),
                    dismissButton: .default(Text(CommonLocalizables().ok.localized), action: dismiss)
                )
            } else {
                return Alert(
                    title: Text(title()),
                    dismissButton: .default(Text(CommonLocalizables().ok.localized), action: dismiss)
                )
            }
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
