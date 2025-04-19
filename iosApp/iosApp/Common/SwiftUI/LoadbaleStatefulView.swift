//
//  LoadbaleStatefulView.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

func LoadableStatefulView<
    LoadableData: AnyObject,
    Data,
    Error: LivingLinkError,
    EmptyContent: View,
    LoadingContent: View,
    ErrorContent: View,
    Content: View
>(
    viewModel: LoadableStatefulViewModel,
    buildAlert: @escaping (Error) -> Alert,
    emptyContent: @escaping () -> EmptyContent = {
        EmptyView()
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }
    },
    loadingContent: @escaping () -> LoadingContent = {
        ProgressView()
            .fillMaxSize()
            .background {
                DesignSystem.background
                    .ignoresSafeArea()
            }
    },
    errorContent: @escaping (Error) -> ErrorContent,
    content: @escaping (LoadableData, Data) -> Content
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

fileprivate struct LoadableStatefulViewContent<
    LoadableData: AnyObject,
    Data,
    Error: LivingLinkError,
    EmptyContent: View,
    LoadingContent: View,
    ErrorContent: View,
    Content: View
>: View {
    let viewModel: LoadableStatefulViewModel
    let buildAlert: (Error) -> Alert
    let emptyContent: () -> EmptyContent
    let loadingContent: () -> LoadingContent
    let errorContent: (Error) -> ErrorContent
    let content: (LoadableData, Data) -> Content
    
    @ObservedObject var loadableData: StateFlowObservable<LoadableViewModelStateState<LoadableData, LivingLinkError>>
    @ObservedObject var data: StateFlowObservable<Data>
    @ObservedObject var error: StateFlowObservable<Error?>
    @ObservedObject var loading: StateFlowObservable<KotlinBoolean>
    
    init(
        viewModel: LoadableStatefulViewModel,
        buildAlert: @escaping (Error) -> Alert,
        emptyContent: @escaping () -> EmptyContent,
        loadingContent: @escaping () -> LoadingContent,
        errorContent: @escaping (Error) -> ErrorContent,
        content: @escaping (LoadableData, Data) -> Content
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
                Color.black.opacity(0.2)
                    .ignoresSafeArea()
                
                VStack {
                    ProgressView()
                        .padding()
                }
            }
            
            EmptyView()
                .alert(isPresented: isErrorAlertPresented) {
                    buildAlert(error.value!)
                }
        }
    }
    
    @ViewBuilder
    private func loadableStateContent() -> some View {
        switch loadableData.value {
        case is LoadableViewModelStateStateEmpty<LoadableData, LivingLinkError>:
            emptyContent()
            
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
