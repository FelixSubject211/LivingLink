//
//  GroupDetailScreen.swift
//  iosApp
//
//  Created by Felix Fischer on 08.05.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

struct GroupDetailScreen: View {
    let shoppingListViewModel: ShoppingListListViewModel
    let taskBoardListViewModel: TaskBoardListViewModel
    let groupSettingsViewModel: GroupSettingsViewModel
    let localizables = GroupsDetailScreenLocalizables()

    @State private var selectedTab: Tab = .shopping

    enum Tab {
        case shopping
        case tasks
        case groupSettings
    }

    var body: some View {
        VStack {
            TabView(selection: $selectedTab) {
                ShoppingListListScreen(viewModel: shoppingListViewModel)
                    .tabItem {
                        Label(localizables.tabShoppingList.localized, systemImage: "cart")
                    }
                    .tag(Tab.shopping)

                TaskBoardListScreen(viewModel: taskBoardListViewModel)
                    .tabItem {
                        Label(localizables.tabTaskBoard.localized, systemImage: "checkmark.square")
                    }
                    .tag(Tab.tasks)

                GroupSettingsScreen(viewModel: groupSettingsViewModel)
                    .tabItem {
                        Label(localizables.tapGroupSettings.localized, systemImage: "gearshape")
                    }
                    .tag(Tab.groupSettings)
            }
        }
        .navigationTitle(navigationTitle(for: selectedTab))
        .navigationBarTitleDisplayMode(.inline)
    }

    private func navigationTitle(for tab: Tab) -> String {
        switch tab {
        case .shopping:
            return ShoppingListListScreenLocalizables().navigationTitle.localized
        case .tasks:
            return TaskBoardListScreenLocalizables().navigationTitle.localized
        case .groupSettings:
            return GroupsSettingsScreenLocalizables().navigationTitle.localized
        }
    }
}
