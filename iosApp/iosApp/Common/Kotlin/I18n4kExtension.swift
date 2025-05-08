//
//  I18n4kExtension.swift
//  iosApp
//
//  Created by Felix Fischer on 23.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp

extension I18n4k_coreMessageBundleLocalizedStringFactory1 {
    func localized(_ p0: Any) -> String {
        self.get(p0: p0).invoke()
    }
}


extension I18n4k_coreMessageBundleLocalizedString {
    var localized: String {
        self.invoke()
    }
}
