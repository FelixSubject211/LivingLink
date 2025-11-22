package felix.projekt.livinglink.composeApp.shoppingList.interfaces

interface CreateShoppingListItemUseCase {
    suspend operator fun invoke(groupId: String, name: String): Response

    sealed class Response {
        data object Success : Response()
        data object NetworkError : Response()
    }
}