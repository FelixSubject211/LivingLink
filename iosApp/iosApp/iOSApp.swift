import ComposeApp
import SwiftUI

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

private struct NavigationView: View {
    let uiModule: UiModule
    @StateObject var navigartor: IosNavigator

    var body: some View {
        NavigationStack(path: $navigartor.navigationPath) {
            GroupListScreen(viewModel: uiModule.groupListViewModel)
                .navigationDestination(for: IosNavigator.Screen.GroupDetail.self) { group in
                    let groupId = group.groupId
                    let groupDetailViewModel = ViewModelCache.getOrCreate(key: "groupViewModel_\(groupId)") {
                        uiModule.groupDetailViewModel(groupId: groupId)
                    }
                    let shoppingListViewModel = ViewModelCache.getOrCreate(key: "shoppingListViewModel_\(groupId)") {
                        uiModule.shoppingListViewModel(groupId: groupId)
                    }
                    GroupDetailScreen(
                        groupDetailViewModel: groupDetailViewModel,
                        shoppingListViewModel: shoppingListViewModel
                    )
                }
                .navigationDestination(for: IosNavigator.Screen.Settings.self) { _ in
                    SettingsScreen(viewModel: uiModule.settingsViewModel)
                }
                .navigationDestination(for: IosNavigator.Screen.Login.self) { _ in
                    let loginViewModel = ViewModelCache.getOrCreate(key: "loginViewModel") {
                        uiModule.loginViewModel()
                    }
                    LoginScreen(viewModel: loginViewModel)
                }
                .navigationDestination(for: IosNavigator.Screen.Register.self) { _ in
                    let registerViewModel = ViewModelCache.getOrCreate(key: "registerViewModel") {
                        uiModule.registerViewModel()
                    }
                    RegisterScreen(viewModel: registerViewModel)
                }
                .navigationDestination(for: IosNavigator.Screen.ShoppingListItem.self) {
                    let groupId = $0.groupId
                    let itemId = $0.itemId
                    let viewModel = ViewModelCache.getOrCreate(key: "shoppingListItemViewModel_\(groupId)_\(itemId)") {
                        uiModule.shoppingListItemViewModel(groupId: groupId, itemId: itemId)
                    }
                    ShoppingDetailItemScreen(viewModel: viewModel)
                }
        }
        .onChange(of: navigartor.navigationPath, initial: false) { oldPath, newPath in
            let oldCount = oldPath.count
            let newCount = newPath.count

            if newCount == 0 {
                ViewModelCache.clearAll()
            }

            if newCount < oldCount {
                for _ in newCount ..< oldCount {
                    navigartor.currentGroupIdObserver?.pop()
                }
            }
        }
    }

    class ViewModelCache {
        private static var cache: [String: Any] = [:]

        static func getOrCreate<T>(key: String, factory: () -> T) -> T {
            if let existing = cache[key] as? T {
                return existing
            } else {
                let instance = factory()
                cache[key] = instance
                return instance
            }
        }

        static func clearAll() {
            for value in cache.values {
                if let cancellable = value as? StatefulViewModel {
                    cancellable.cancel()
                } else if let cancellable = value as? LoadableStatefulViewModel {
                    cancellable.cancel()
                }
            }
            cache.removeAll()
        }
    }
}
