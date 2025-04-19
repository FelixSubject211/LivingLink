//
//  StateFlowCollector.swift
//  iosApp
//
//  Created by Felix Fischer on 19.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp

class StateFlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    let onValueEmitted: (T) -> Void

    init(onValueEmitted: @escaping (T) -> Void) {
        self.onValueEmitted = onValueEmitted
    }

    func emit(value: Any?) async throws {
        onValueEmitted(value as! T)
    }
}
