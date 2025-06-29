//
//  KotlinFlowExtension.swift
//  iosApp
//
//  Created by Felix Fischer on 29.06.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

extension Kotlinx_coroutines_coreFlow {
    func asObservableObject<T>() -> FlowObservable<T> {
        FlowObservable<T>(flow: self)
    }
}

class FlowObservable<T>: ObservableObject {
    @Published var value: T?
    private var flow: Kotlinx_coroutines_coreFlow

    init(flow: Kotlinx_coroutines_coreFlow) {
        self.flow = flow
        observeStateFlow()
    }

    private func observeStateFlow() {
        flow.collect(
            collector: StateFlowCollector { [weak self] newValue in
                DispatchQueue.main.async {
                    withAnimation {
                        self?.value = newValue
                    }
                }
            }
        ) { _ in }
    }
}
