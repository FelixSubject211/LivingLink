package felix.projekt.livinglink.composeApp.shoppingList.di

import felix.projekt.livinglink.composeApp.shoppingList.application.CheckShoppingListItemDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.CreateShoppingListItemDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.DeleteShoppingListItemDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.GetShoppingListItemHistoryDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.GetShoppingListStateDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.UncheckShoppingListItemDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.CheckShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.CreateShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.DeleteShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListItemHistoryUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListStateUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.UncheckShoppingListItemUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val shoppingListModule = module {
    factoryOf(::GetShoppingListStateDefaultUseCase) bind GetShoppingListStateUseCase::class
    factoryOf(::GetShoppingListItemHistoryDefaultUseCase) bind GetShoppingListItemHistoryUseCase::class
    factoryOf(::CreateShoppingListItemDefaultUseCase) bind CreateShoppingListItemUseCase::class
    factoryOf(::CheckShoppingListItemDefaultUseCase) bind CheckShoppingListItemUseCase::class
    factoryOf(::UncheckShoppingListItemDefaultUseCase) bind UncheckShoppingListItemUseCase::class
    factoryOf(::DeleteShoppingListItemDefaultUseCase) bind DeleteShoppingListItemUseCase::class
}