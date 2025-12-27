package felix.projekt.livinglink.composeApp.groups.domain

interface GroupsStore {
    fun getGroups(): Map<String, Group>
    fun saveGroups(groups: Map<String, Group>)
    fun clear()
}