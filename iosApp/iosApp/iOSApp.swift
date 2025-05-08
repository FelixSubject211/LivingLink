import SwiftUI
import ComposeApp

import SwiftUI

@main
struct iOSApp: App {
    
    let navigator: IosNavigator
    let uiModule: UiModule
    
    init() {
        navigator = IosNavigator()
        uiModule = AppModuleKt.defaultAppModule(navigator: navigator).uiModule
        
        InitI18n4kKt.doInitI18n4k()
        
        UIView.appearance(
            whenContainedInInstancesOf: [UIAlertController.self]
        ).tintColor = UIColor(DesignSystem.Colors.primary)
    }
    
    var body: some Scene {
        
        WindowGroup {
            NavigationView(
                uiModule: uiModule,
                navigartor: navigator
            )
            .tint(DesignSystem.Colors.primary)
        }
    }
}

fileprivate struct NavigationView: View {
    let uiModule: UiModule
    @StateObject var navigartor: IosNavigator

    var body: some View {
        NavigationStack(path: $navigartor.navigationPath) {
            ListGroupsScreen(viewModel: uiModule.listGroupsViewModel)
                .navigationDestination(for: IosNavigator.Screen.Group.self) { group in
                    GroupScreen(viewModel: uiModule.groupViewModel(groupId: group.groupId))
                }
                .navigationDestination(for: IosNavigator.Screen.Settings.self) { _ in
                    SettingsScreen(viewModel: uiModule.settingsViewModel)
                }
                .navigationDestination(for: IosNavigator.Screen.Login.self) { _ in
                    LoginScreen(viewModel: uiModule.loginViewModel())
                }
                .navigationDestination(for: IosNavigator.Screen.Register.self) { _ in
                    RegisterScreen(viewModel: uiModule.registerViewModel())
                }
        }
    }
}
