package felix.projekt.livinglink.gatling.dsl

import felix.projekt.livinglink.gatling.common.SessionKeys
import felix.projekt.livinglink.gatling.common.authHeader
import felix.projekt.livinglink.shared.groups.requestModel.GroupRequest
import felix.projekt.livinglink.shared.json
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

fun getGroups() = http("Get Groups")
    .post("/groups")
    .header("Authorization") { session ->
        authHeader(session.getString(SessionKeys.accessToken)!!)
    }
    .body(StringBody { session ->
        json.encodeToString(
            GroupRequest.GetGroups(
                currentGroupVersions = emptyMap()
            )
        )
    })
    .check(status().`is`(200))

fun createGroup(groupName: String) = http("Create Group")
    .post("/groups/create")
    .header("Authorization") { session ->
        authHeader(session.getString(SessionKeys.accessToken)!!)
    }
    .body(StringBody {
        json.encodeToString(GroupRequest.CreateGroup(groupName))
    })
    .check(status().`is`(200))
    .check(jsonPath("$.group.id").saveAs(SessionKeys.groupId))


fun createInviteCode(groupIdKey: String, inviteName: String) = http("Create InviteCode")
    .post("/groups/inviteCode/create")
    .header("Authorization") { session ->
        authHeader(session.getString(SessionKeys.accessToken)!!)
    }
    .body(StringBody { session ->
        json.encodeToString(
            GroupRequest.CreateInviteCode(
                groupId = session.getString(groupIdKey)!!,
                inviteCodeName = inviteName
            )
        )
    })
    .check(status().`is`(200))
    .check(jsonPath("$.key").saveAs(SessionKeys.inviteCode))


fun deleteInviteCode(groupIdKey: String, inviteCodeIdKey: String) = http("Delete InviteCode")
    .post("/groups/inviteCode/delete")
    .header("Authorization") { session ->
        authHeader(session.getString(SessionKeys.accessToken)!!)
    }
    .body(StringBody { session ->
        json.encodeToString(
            GroupRequest.DeleteInviteCode(
                groupId = session.getString(groupIdKey)!!,
                inviteCodeId = session.getString(inviteCodeIdKey)!!
            )
        )
    })
    .check(status().`is`(200))


fun joinGroup(inviteKeySession: String) = http("Join Group")
    .post("/groups/inviteCode/join")
    .header("Authorization") { session ->
        authHeader(session.getString(SessionKeys.accessToken)!!)
    }
    .body(StringBody { session ->
        json.encodeToString(
            GroupRequest.JoinGroup(
                inviteCodeKey = session.getString(inviteKeySession)!!
            )
        )
    })
    .check(status().`is`(200))