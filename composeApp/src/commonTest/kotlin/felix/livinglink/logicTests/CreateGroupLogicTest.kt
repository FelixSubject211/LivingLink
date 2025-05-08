package felix.livinglink.logicTests

import app.cash.turbine.turbineScope
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.defaultAppTestModule
import felix.livinglink.expectStates
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.Group
import felix.livinglink.groups.network.GroupsNetworkDataSource
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.state.LoadableViewModelState
import felix.livinglink.ui.listGroups.ListGroupsViewModel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CreateGroupLogicTest {
    private lateinit var mockGroupsNetworkDataSource: GroupsNetworkDataSource
    private lateinit var appTestModule: UiModule

    @BeforeTest
    fun setup() {
        mockGroupsNetworkDataSource = mock(mode = MockMode.autofill)
        appTestModule = defaultAppTestModule(
            groupNetworkDataSource = mockGroupsNetworkDataSource
        )
    }

    @Test
    fun `test create group on success`() = runTest {

        val group = Group(
            id = "id",
            name = "testGroup",
            groupMemberIdsToName = emptyMap(),
            createdAt = Clock.System.now()
        )

        // Arrange
        everySuspend {
            mockGroupsNetworkDataSource.createGroup(CreateGroupRequest(group.name))
        } returns LivingLinkResult.Success(CreateGroupResponse.Success(groupId = group.id))

        everySuspend {
            mockGroupsNetworkDataSource.getGroupsForUser()
        } returns LivingLinkResult.Success(GetGroupsForUserResponse(groups = setOf(group)))

        val viewModel = appTestModule.listGroupsViewModel

        turbineScope {
            val loadableData = viewModel.loadableData.testIn(backgroundScope)
            val loading = viewModel.loading.testIn(backgroundScope)
            val error = viewModel.error.testIn(backgroundScope)

            // Act
            viewModel.createGroup(group.name)

            // Assert
            loadableData.expectStates(
                LoadableViewModelState.State.Loading(),
                LoadableViewModelState.State.Empty(),
                LoadableViewModelState.State.Loading(),
                LoadableViewModelState.State.Data(
                    data = ListGroupsViewModel.LoadableData(
                        groups = listOf(group)
                    )
                )
            )

            assertFalse(loading.awaitItem())

            assertNull(error.awaitItem())
        }

        // Assert - side effects
        verifySuspend(exhaustiveOrder) {
            mockGroupsNetworkDataSource.createGroup(CreateGroupRequest(group.name))
            mockGroupsNetworkDataSource.getGroupsForUser()
        }
        verifyNoMoreCalls(mockGroupsNetworkDataSource)
    }
}