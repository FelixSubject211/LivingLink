package com.felix.livinglink.composeapp.groups.application

import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import org.koin.core.annotation.Single

interface SelectGroupUseCase {
    operator fun invoke(groupId: String)
}

@Single(binds = [SelectGroupUseCase::class])
class SelectGroupDefaultUseCase(
    private val groupsRepository: GroupsRepository,
) : SelectGroupUseCase {
    override fun invoke(groupId: String) = groupsRepository.selectGroup(groupId)
}