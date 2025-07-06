//
//  IosNavigator.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

class IosNavigator: Navigator, ObservableObject {
    @Published var navigationPath = NavigationPath()

    var currentGroupIdObserver: EventBusCurrentGroupIdObserver?

    func addObserver(currentGroupIdObserver: EventBusCurrentGroupIdObserver) {
        self.currentGroupIdObserver = currentGroupIdObserver
    }

    func pop() {
        DispatchQueue.main.async {
            self.currentGroupIdObserver?.pop()
            self.navigationPath.removeLast()
        }
    }

    func popAll() {
        DispatchQueue.main.async {
            self.currentGroupIdObserver?.popAll()
            self.navigationPath.removeLast(self.navigationPath.count)
        }
    }

    func push(screen: LivingLinkScreen) {
        DispatchQueue.main.async {
            self.currentGroupIdObserver?.push(screen: screen)
            switch screen {
            case is LivingLinkScreen.GroupList:
                self.navigationPath.append(Screen.GroupList())
            case is LivingLinkScreen.GroupDetail:
                let groupId = (screen as! LivingLinkScreen.GroupDetail).groupId
                self.navigationPath.append(Screen.GroupDetail(groupId: groupId))
            case is LivingLinkScreen.Settings:
                self.navigationPath.append(Screen.Settings())
            case is LivingLinkScreen.Login:
                self.navigationPath.append(Screen.Login())
            case is LivingLinkScreen.Register:
                self.navigationPath.append(Screen.Register())
            case is LivingLinkScreen.ShoppingListItem:
                let screen = screen as! LivingLinkScreen.ShoppingListItem
                self.navigationPath.append(Screen.ShoppingListItem(
                    groupId: screen.groupId,
                    itemId: screen.itemId
                ))
            default: break
            }
        }
    }

    func canNavigateBack() -> Bool {
        navigationPath.count > 0
    }

    enum Screen {
        struct GroupList: Hashable {}
        struct GroupDetail: Hashable {
            let groupId: String
        }

        struct Settings: Hashable {}
        struct Login: Hashable {}
        struct Register: Hashable {}
        struct ShoppingListItem: Hashable {
            let groupId: String
            let itemId: String
        }
    }
}
