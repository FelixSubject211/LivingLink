//
//  ViewExtension.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

extension View {
    func fillMaxSize(
        alignment: Alignment = .center
    ) -> some View {
        self
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: alignment)
    }
}
