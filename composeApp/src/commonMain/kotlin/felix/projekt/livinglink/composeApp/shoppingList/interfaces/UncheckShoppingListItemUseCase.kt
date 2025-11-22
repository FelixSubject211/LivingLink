package felix.projekt.livinglink.composeApp.shoppingList.interfaces

interface UncheckShoppingListItemUseCase {
    suspend operator fun invoke(groupId: String, itemId: String): Response

    sealed class Response {
        data object Success : Response()
        data object AlreadyUnchecked : Response()
        data object ItemNotFound : Response()
        data object NetworkError : Response()
    }
}
