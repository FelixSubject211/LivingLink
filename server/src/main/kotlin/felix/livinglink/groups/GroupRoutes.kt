package felix.livinglink.groups

import felix.livinglink.common.UserPrincipal
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.UseInviteRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.groupRoutes(groupService: GroupService) {
    route("/groups") {

        get("/get") {
            val user = call.principal<UserPrincipal>()!!
            val response = groupService.getGroupsForUser(user.userId)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/create") {
            val user = call.principal<UserPrincipal>()!!
            val request = call.receive<CreateGroupRequest>()
            val response = groupService.createGroup(
                userId = user.userId,
                request = request
            )
            call.respond(HttpStatusCode.Created, response)
        }

        delete("/{groupId}") {
            val user = call.principal<UserPrincipal>()!!
            val groupId = call.parameters["groupId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val response = groupService.deleteGroup(groupId = groupId, userId = user.userId)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/invite/create") {
            val user = call.principal<UserPrincipal>()!!
            val request = call.receive<CreateInviteRequest>()
            val response = groupService.createInviteCode(request, user.userId)
            if (response != null) {
                call.respond(HttpStatusCode.OK, response)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }

        post("/invite/use") {
            val user = call.principal<UserPrincipal>()!!
            val request = call.receive<UseInviteRequest>()
            val response = groupService.useInviteCode(request, user.userId)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}