//
//  Navigator.swift
//  iosApp
//
//  Created by Felix Fischer on 26.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
import SwiftUI

class IosNavigator: Navigator, ObservableObject {
    @Published var navigationPath = NavigationPath()
    
    func pop() {
        DispatchQueue.main.async {
            self.navigationPath.removeLast()
        }
    }
    
    func popAll() {
        DispatchQueue.main.async {
            self.navigationPath.removeLast(self.navigationPath.count)
        }
    }
    
    func pushLoginScreen() {
        DispatchQueue.main.async {
            self.navigationPath.append(LivingLinkScreen.Login())
        }
    }
    
    func push(screen: LivingLinkScreen) {
        DispatchQueue.main.async {
            switch(screen) {
            case is LivingLinkScreen.ListGroups:
                self.navigationPath.append(Screen.ListGroups())
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
        struct ListGroups: Hashable{}
        struct Settings: Hashable{}
        struct Login: Hashable{}
        struct Register: Hashable{}
    }
}
