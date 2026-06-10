package com.felix.livinglink.shared.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupDtoV1(
    val id: String,
    val name: String,
)