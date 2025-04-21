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
    emptyContent: @escaping (Data) -> AnyView = { defaultEmptyContent($0) },
    loadingContent: @escaping () -> AnyView = defaultLoadingContent,
    errorContent: @escaping (Error) -> AnyView,
    content: @escaping (LoadableData, Data) -> AnyView
) -> some View {
    LoadableStatefulViewContent(
        viewModel: viewModel,
        buildAlert: buildAlert,
        emptyContent: emptyContent,
        loadingContent: loadingContent,
        errorContent: errorContent,
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

fileprivate struct LoadableStatefulViewContent<LoadableData: AnyObject, Data, Error: LivingLinkError>: View {
    let viewModel: LoadableStatefulViewModel
    let buildAlert: (Error) -> Alert
    let emptyContent: (Data) -> AnyView
    let loadingContent: () ->  AnyView
    let errorContent: (Error) ->  AnyView
    let content: (LoadableData, Data) ->  AnyView
    
    @ObservedObject var loadableData: StateFlowObservable<LoadableViewModelStateState<LoadableData, LivingLinkError>>
    @ObservedObject var data: StateFlowObservable<Data>
    @ObservedObject var error: StateFlowObservable<Error?>
    @ObservedObject var loading: StateFlowObservable<KotlinBoolean>
    
    init(
        viewModel: LoadableStatefulViewModel,
        buildAlert: @escaping (Error) -> Alert,
        emptyContent: @escaping (Data) -> AnyView,
        loadingContent: @escaping () -> AnyView,
        errorContent: @escaping (Error) -> AnyView,
        content: @escaping (LoadableData, Data) -> AnyView
    ){
        self.viewModel = viewModel
        self.loadableData = viewModel.loadableData.asObservableObject()
        self.data = viewModel.data.asObservableObject()
        self.error = viewModel.error.asObservableObject()
        self.loading = viewModel.loading.asObservableObject()
        
        self.buildAlert = buildAlert
        self.emptyContent = emptyContent
        self.loadingContent = loadingContent
        self.errorContent = errorContent
        self.content = content
    }
    
    @ViewBuilder
    var body: some View {
        
        ZStack {
            loadableStateContent()
                .disabled(loading.value.boolValue)
            
            if (loading.value.boolValue) {
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
            emptyContent(data.value)
            
        case is LoadableViewModelStateStateLoading<LoadableData, LivingLinkError>:
            loadingContent()
            
        case let errorState as LoadableViewModelStateStateError<LoadableData, LivingLinkError>:
            if let error = errorState.error as? Error {
                errorContent(error)
            } else {
                loadingContent()
            }
            
        case let dataState as LoadableViewModelStateStateData<LoadableData, LivingLinkError>:
            if let stateData = dataState.data {
                content(stateData, data.value)
            } else {
                loadingContent()
            }
            
        default:
            loadingContent()
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
