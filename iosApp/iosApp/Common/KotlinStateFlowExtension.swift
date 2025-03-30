//
//  KotlinStateFlow+AsObservableObject.swift
//  iosApp
//
//  Created by Felix Fischer on 22.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI
import Combine

extension Kotlinx_coroutines_coreStateFlow {
    func asObservableObject<T>() -> StateFlowObservable<T> {
        StateFlowObservable<T>(stateFlow: self)
    }
}

class StateFlowObservable<T>: ObservableObject {
    @Published var value: T
    private var stateFlow: Kotlinx_coroutines_coreStateFlow

    init(stateFlow: Kotlinx_coroutines_coreStateFlow) {
        self.stateFlow = stateFlow
        self.value = stateFlow.value as! T
        observeStateFlow()
    }

    private func observeStateFlow() {
        stateFlow.collect(
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

fileprivate class StateFlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    let onValueEmitted: (T) -> Void

    init(onValueEmitted: @escaping (T) -> Void) {
        self.onValueEmitted = onValueEmitted
    }

    func emit(value: Any?) async throws {
        onValueEmitted(value as! T)
    }
}

