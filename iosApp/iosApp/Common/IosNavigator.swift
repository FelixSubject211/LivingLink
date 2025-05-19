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

    private var currentGroupIdObserver: EventBusCurrentGroupIdObserver?

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
            case is LivingLinkScreen.ListGroups:
                self.navigationPath.append(Screen.ListGroups())
            case is LivingLinkScreen.Group:
                self.navigationPath.append(Screen.Group(groupId: (screen as! LivingLinkScreen.Group).groupId))
            case is LivingLinkScreen.Settings:
                self.navigationPath.append(Screen.Settings())
            case is LivingLinkScreen.Login:
                self.navigationPath.append(Screen.Login())
            case is LivingLinkScreen.Register:
                self.navigationPath.append(Screen.Register())
            default: break
            }
        }
    }

    enum Screen {
        struct ListGroups: Hashable {}
        struct Group: Hashable { let groupId: String }
        struct Settings: Hashable {}
        struct Login: Hashable {}
        struct Register: Hashable {}
    }
}
