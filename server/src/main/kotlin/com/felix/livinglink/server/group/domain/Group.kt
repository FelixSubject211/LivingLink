package com.felix.livinglink.server.group.domain

data class Group(
    val id: String,
    val name: String,
    val memberUserIds: Set<String>,
)
