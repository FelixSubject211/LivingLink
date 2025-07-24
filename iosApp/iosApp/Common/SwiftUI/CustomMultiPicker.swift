//
//  CustomMultiPicker.swift
//  iosApp
//
//  Created by Felix Fischer on 23.07.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

public struct CustomMultiPicker<SelectionValue: Hashable, Label: View, Content: View>: View {
    private let title: String
    private let options: [SelectionValue]
    private let selected: [SelectionValue]
    private let valueToString: (SelectionValue) -> String
    private let label: () -> Label
    private let row: (SelectionValue) -> Content
    private let onSelectionChanged: (SelectionValue) -> Void

    public init(
        title: String,
        options: [SelectionValue],
        selected: [SelectionValue],
        valueToString: @escaping (SelectionValue) -> String,
        @ViewBuilder label: @escaping () -> Label,
        onSelectionChanged: @escaping (SelectionValue) -> Void
    ) where Content == Text {
        self.title = title
        self.options = options
        self.selected = selected
        self.valueToString = valueToString
        self.label = label
        self.onSelectionChanged = onSelectionChanged
        row = { value in
            Text(valueToString(value))
        }
    }

    public var body: some View {
        NavigationLink {
            List {
                ForEach(options, id: \.self) { option in
                    Button {
                        onSelectionChanged(option)
                    } label: {
                        HStack {
                            row(option)
                            Spacer()
                            if selected.contains(option) {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.accentColor)
                            }
                        }
                    }
                }
            }
            .listStyle(.plain)
            .background(DesignSystem.background.ignoresSafeArea())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    label()
                        .font(.headline)
                }
            }
        } label: {
            LabeledContent {
                if selected.isEmpty {
                    Text("-")
                } else {
                    Text(selected.map { valueToString($0) }.formatted(.list(type: .and)))
                }
            } label: {
                label()
            }
        }
    }
}
