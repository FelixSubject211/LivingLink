package com.felix.livinglink.shared.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
)