//
//  StatefulViewModelView.swift
//  iosApp
//
//  Created by Felix Fischer on 22.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI


struct StatefulView<Data, Error: LivingLinkError, Content: View>: View {
    let viewModel: StatefulViewModel
    let buildAlert: (Error) -> Alert
    let content: (Data) -> Content
    
    @ObservedObject var data: StateFlowObservable<Data>
    @ObservedObject var error: StateFlowObservable<Error?>
    @ObservedObject var loading: StateFlowObservable<KotlinBoolean>
    
    init(
        viewModel: StatefulViewModel,
        buildAlert: @escaping (Error) -> Alert,
        content: @escaping (Data) -> Content
    ) {
        self.viewModel = viewModel
        self.data = viewModel.data.asObservableObject()
        self.error = viewModel.error.asObservableObject()
        self.loading = viewModel.loading.asObservableObject()
        
        self.buildAlert = buildAlert
        self.content = content
    }
    
    var body: some View {
        ZStack {
            content(data.value)
                .disabled(loading.value.boolValue)
            
            if (loading.value.boolValue) {
                Color.black.opacity(0.2)
                    .ignoresSafeArea()
                
                VStack {
                    ProgressView()
                        .padding()
                }
            }
        }.alert(isPresented: isErrorAlertPresented) {
            buildAlert(error.value!)
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
