package felix.projekt.livinglink.composeApp.shoppingList.interfaces

interface CheckShoppingListItemUseCase {
    suspend operator fun invoke(groupId: String, itemId: String): Response

    sealed class Response {
        data object Success : Response()
        data object AlreadyChecked : Response()
        data object ItemNotFound : Response()
        data object NetworkError : Response()
    }
}