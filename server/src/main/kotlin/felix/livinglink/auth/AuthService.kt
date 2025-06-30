package felix.livinglink.auth

import felix.livinglink.common.TimeService
import felix.livinglink.common.UuidFactory
import felix.livinglink.event.ChangeNotifier
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.EventSourcingStore
import felix.livinglink.eventSourcing.UserAnonymized
import felix.livinglink.groups.GroupStore
import felix.livinglink.json
import kotlinx.datetime.Instant
import kotlinx.serialization.PolymorphicSerializer

class AuthService(
    private val userStore: UserStore,
    private val groupStore: GroupStore,
    private val eventSourcingStore: EventSourcingStore,
    private val passwordHasherService: PasswordHasherService,
    private val jwtService: JwtService,
    private val changeNotifier: ChangeNotifier,
    private val timeService: TimeService,
    private val uuidFactory: UuidFactory
) {
    fun registerUser(request: RegisterRequest): RegisterResponse {
        if (request.username.length < 8) {
            return RegisterResponse.UsernameTooShort(minLength = 8)
        }
        if (request.password.length < 8) {
            return RegisterResponse.PasswordTooShort(minLength = 8)
        }

        val hashedPassword = passwordHasherService.hashPassword(request.password)
        val newUserId = userStore.addUser(
            userId = uuidFactory(),
            username = request.username,
            hashedPassword = hashedPassword
        ) ?: return RegisterResponse.UserAlreadyExists

        val accessToken = jwtService.generateToken(
            userId = newUserId,
            username = request.username,
            groupIds = emptyList()
        )
        val refreshToken = jwtService.generateRefreshToken(
            userId = newUserId,
            username = request.username
        )

        userStore.storeRefreshToken(newUserId, refreshToken)

        return RegisterResponse.Success(accessToken, refreshToken.token)
    }

    fun loginUser(request: LoginRequest): LoginResponse {
        val user = userStore.getUserByUsername(request.username)
        if (user != null && passwordHasherService.verifyPassword(
                request.password,
                user.hashedPassword
            )
        ) {
            val accessToken = jwtService.generateToken(
                userId = user.id,
                username = request.username,
                groupIds = groupStore.getUserIdsInGroup(user.id)
            )
            val refreshToken = jwtService.generateRefreshToken(
                userId = user.id,
                username = user.username
            )
            userStore.storeRefreshToken(user.id, refreshToken)
            return LoginResponse.Success(accessToken, refreshToken.token)
        } else {
            return LoginResponse.InvalidUsernameOrPassword
        }
    }

    fun refreshAccessToken(request: RefreshTokenRequest): RefreshTokenResponse {
        val storedToken = userStore.getRefreshToken(request.refreshToken)
        return if (storedToken == null || storedToken.expiresAt < timeService.currentTimeMillis()) {
            RefreshTokenResponse.InvalidOrExpiredRefreshToken
        } else {
            val newAccessToken = jwtService.generateToken(
                userId = storedToken.userId,
                username = storedToken.username,
                groupIds = groupStore.getUserIdsInGroup(storedToken.userId)
            )
            val newRefreshToken = jwtService.generateRefreshToken(
                userId = storedToken.userId,
                username = storedToken.username
            )

            userStore.deleteRefreshToken(storedToken.token)
            userStore.storeRefreshToken(storedToken.userId, newRefreshToken)

            RefreshTokenResponse.Success(newAccessToken, newRefreshToken.token)
        }
    }

    fun logoutUser(request: LogoutRequest): LogoutResponse {
        return if (userStore.deleteRefreshToken(request.refreshToken)) {
            LogoutResponse.Success
        } else {
            LogoutResponse.InvalidRefreshToken
        }
    }

    fun deleteUser(userId: String): DeleteUserResponse {
        val now = Instant.fromEpochMilliseconds(timeService.currentTimeMillis())

        val payloadJson = json.encodeToString(
            PolymorphicSerializer(EventSourcingEvent.Payload::class),
            UserAnonymized(originalUserId = userId)
        )

        groupStore.getGroupsForUser(userId).forEach { group ->
            val newEventId = eventSourcingStore.appendEvent(
                groupId = group.id,
                userId = userId,
                eventType = UserAnonymized::class.qualifiedName!!,
                createdAt = now,
                payload = payloadJson
            )!!

            changeNotifier.markEventChangeForGroup(groupId = group.id, eventId = newEventId)

            group.groupMemberIdsToName.keys.forEach { userId ->
                changeNotifier.markGroupChangeForUser(userId = userId)
            }
        }

        eventSourcingStore.anonymizeEventsByUser(userId = userId)

        return if (userStore.deleteUser(userId)) {
            DeleteUserResponse.Success
        } else {
            DeleteUserResponse.Error
        }
    }
}