package com.felix.livinglink.composeapp.groups.application

import com.felix.livinglink.composeapp.groups.domain.GroupState
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

interface ObserveGroupStateUseCase {
    operator fun invoke(): Flow<GroupState>
}

@Single(binds = [ObserveGroupStateUseCase::class])
class ObserveGroupStateDefaultUseCase(
    private val groupsRepository: GroupsRepository,
) : ObserveGroupStateUseCase {
    override fun invoke(): Flow<GroupState> = groupsRepository.state
}