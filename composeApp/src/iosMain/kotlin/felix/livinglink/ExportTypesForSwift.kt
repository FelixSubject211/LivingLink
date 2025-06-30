@file:Suppress("unused")

package felix.livinglink

import felix.livinglink.shoppingList.ShoppingListEvent

/**
 * This file exists solely to force Kotlin/Native to export specific types.
 *
 * Each type is referenced individually to guarantee symbol export.
 */

val _exportItemAdded = ShoppingListEvent.ItemAdded(itemId = "", itemName = "")
val _exportItemCompleted = ShoppingListEvent.ItemCompleted(itemId = "")
val _exportItemUncompleted = ShoppingListEvent.ItemUncompleted(itemId = "")
