package com.felix.livinglink.composeapp.groups.domain

data class GroupsContent(
    val groups: List<Group>,
    val selectedGroup: Group,
)