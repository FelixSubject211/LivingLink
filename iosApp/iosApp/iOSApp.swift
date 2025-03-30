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
            SettingsScreen(viewModel: uiModule.settingsViewModel)
                .navigationDestination(for: IosNavigator.Screen.Login.self) { _ in
                    LoginScreen(viewModel: uiModule.loginViewModel())
                }
                .navigationDestination(for: IosNavigator.Screen.Register.self) { _ in
                    RegisterScreen(viewModel: uiModule.registerViewModel())
                }
        }
    }
}
