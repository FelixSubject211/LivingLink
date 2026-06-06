package com.felix.livinglink.server.group.domain

interface GroupProvider {
    fun groupsById(): Map<String, Group>
}
