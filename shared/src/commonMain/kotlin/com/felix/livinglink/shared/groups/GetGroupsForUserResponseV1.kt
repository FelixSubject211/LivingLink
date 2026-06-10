package com.felix.livinglink.shared.groups

import kotlinx.serialization.Serializable

@Serializable
data class GetGroupsForUserResponseV1(
    val groups: List<GroupDtoV1>,
)