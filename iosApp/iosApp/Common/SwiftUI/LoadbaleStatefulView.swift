//
//  LoadbaleStatefulView.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

func LoadableStatefulView<LoadableData: AnyObject, Data, Error: LivingLinkError>(
    viewModel: LoadableStatefulViewModel,
    buildAlert: @escaping (Error) -> Alert,
    emptyContent: @escaping (Data) -> any View = { defaultEmptyContent($0) },
    loadingContent: @escaping () -> any View = defaultLoadingContent,
    content: @escaping (LoadableData, Data) -> any View
) -> some View {
    LoadableStatefulViewContent(
        viewModel: viewModel,
        buildAlert: buildAlert,
        emptyContent: emptyContent,
        loadingContent: loadingContent,
        content: content
    )
}

private func defaultEmptyContent<Data>(_ _: Data) -> AnyView {
    EmptyView()
        .fillMaxSize()
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
        .eraseToAnyView()
}

private func defaultLoadingContent() -> AnyView {
    ProgressView()
        .fillMaxSize()
        .background {
            DesignSystem.background
                .ignoresSafeArea()
        }
        .eraseToAnyView()
}

private struct LoadableStatefulViewContent<LoadableData: AnyObject, Data, Error: LivingLinkError>: View {
    let viewModel: LoadableStatefulViewModel
    let buildAlert: (Error) -> Alert
    let emptyContent: (Data) -> any View
    let loadingContent: () -> any View
    let content: (LoadableData, Data) -> any View

    @ObservedObject var loadableData: StateFlowObservable<LoadableViewModelStateState<LoadableData, LivingLinkError>>
    @ObservedObject var data: StateFlowObservable<Data>
    @ObservedObject var error: StateFlowObservable<Error?>
    @ObservedObject var loading: StateFlowObservable<KotlinBoolean>

    init(
        viewModel: LoadableStatefulViewModel,
        buildAlert: @escaping (Error) -> Alert,
        emptyContent: @escaping (Data) -> any View,
        loadingContent: @escaping () -> any View,
        content: @escaping (LoadableData, Data) -> any View
    ) {
        self.viewModel = viewModel
        loadableData = viewModel.loadableData.asObservableObject()
        data = viewModel.data.asObservableObject()
        error = viewModel.error.asObservableObject()
        loading = viewModel.loading.asObservableObject()

        self.buildAlert = buildAlert
        self.emptyContent = emptyContent
        self.loadingContent = loadingContent
        self.content = content
    }

    @ViewBuilder
    var body: some View {
        ZStack {
            loadableStateContent()
                .disabled(loading.value.boolValue)

            if loading.value.boolValue {
                VStack {
                    ProgressView()
                        .padding()
                }
            }

            EmptyView()
        }
        .alert(isPresented: isErrorAlertPresented) {
            buildAlert(error.value!)
        }
    }

    @ViewBuilder
    private func loadableStateContent() -> some View {
        switch loadableData.value {
        case is LoadableViewModelStateStateEmpty<LoadableData, LivingLinkError>:
            emptyContent(data.value).eraseToAnyView()

        case is LoadableViewModelStateStateLoading<LoadableData, LivingLinkError>:
            loadingContent().eraseToAnyView()

        case let dataState as LoadableViewModelStateStateData<LoadableData, LivingLinkError>:
            if let stateData = dataState.data {
                content(stateData, data.value).eraseToAnyView()
            } else {
                loadingContent().eraseToAnyView()
            }

        default:
            loadingContent().eraseToAnyView()
        }
    }

    private var isErrorAlertPresented: Binding<Bool> {
        Binding(
            get: { error.value != nil },
            set: { show in
                if !show {
                    viewModel.closeError()
                }
            }
        )
    }
}
