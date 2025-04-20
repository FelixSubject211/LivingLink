package felix.livinglink.groups.store

import felix.livinglink.common.store.createStore
import felix.livinglink.group.Group
import io.github.xxfast.kstore.extensions.updatesOrEmpty
import kotlinx.coroutines.flow.Flow

interface GroupStore {
    val groups: Flow<List<Group>>
    suspend fun update(newGroups: List<Group>)
}

class GroupDefaultStore : GroupStore {
    private val kStore = createStore<List<Group>>(
        path = "groups",
        defaultValue = emptyList()
    )

    override val groups = kStore.updatesOrEmpty

    override suspend fun update(newGroups: List<Group>) = kStore.update { newGroups }
}