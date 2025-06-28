//
//  StatefulView.swift
//  iosApp
//
//  Created by Felix Fischer on 22.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct StatefulView<Data, Error: LivingLinkError>: View {
    let viewModel: StatefulViewModel
    let buildAlert: (Error) -> Alert
    let content: (Data) -> any View

    @ObservedObject var data: StateFlowObservable<Data>
    @ObservedObject var error: StateFlowObservable<Error?>
    @ObservedObject var loading: StateFlowObservable<KotlinBoolean>

    init(
        viewModel: StatefulViewModel,
        buildAlert: @escaping (Error) -> Alert,
        content: @escaping (Data) -> any View
    ) {
        self.viewModel = viewModel
        data = viewModel.data.asObservableObject()
        error = viewModel.error.asObservableObject()
        loading = viewModel.loading.asObservableObject()

        self.buildAlert = buildAlert
        self.content = content
    }

    var body: some View {
        ZStack {
            content(data.value)
                .eraseToAnyView()
                .disabled(loading.value.boolValue)

            if loading.value.boolValue {
                VStack {
                    ProgressView()
                        .padding()
                }
            }

            InvisibleAlertHost(
                error: Binding(
                    get: { error.value },
                    set: { newValue in
                        if newValue == nil {
                            viewModel.closeError()
                        }
                    }
                ),
                buildAlert: buildAlert,
                onDismiss: viewModel.closeError
            )
        }
    }
}
