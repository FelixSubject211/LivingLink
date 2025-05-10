package felix.livinglink.common

import felix.livinglink.auth.RefreshToken
import felix.livinglink.group.Group
import kotlinx.datetime.Instant

object TestData {
    val fixedTime: Instant = Instant.parse("2020-01-01T00:00:00Z")

    val alice = RawUser(
        id = "alice-id",
        username = "alice",
        password = "password1"
    )

    val bob = RawUser(
        id = "bob-id",
        username = "bob",
        password = "password2"
    )

    val refreshTokenAlice = RefreshToken(
        token = "alice-refresh-token",
        userId = alice.id,
        username = alice.username,
        expiresAt = fixedTime.toEpochMilliseconds()
    )

    val groupOwnedByAlice1 = Group(
        id = "group-alice-1",
        name = "Study Group",
        groupMemberIdsToName = mapOf(alice.id to alice.username),
        createdAt = fixedTime
    )

    val groupOwnedByAlice2 = Group(
        id = "group-alice-2",
        name = "Project Team",
        groupMemberIdsToName = mapOf(alice.id to alice.username),
        createdAt = fixedTime
    )

    val groupOwnedByBob = Group(
        id = "group-bob-1",
        name = "Gaming Buddies",
        groupMemberIdsToName = mapOf(bob.id to bob.username),
        createdAt = fixedTime
    )
}