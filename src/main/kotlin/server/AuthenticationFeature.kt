package server

import exceptions.UnauthorizedException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import models.UserSession
import utils.Clock

suspend fun PipelineContext<Unit, ApplicationCall>.setUserSession() {
    val userName = call.principal<UserIdPrincipal>()?.name.toString()
    call.sessions.set(UserSession(name = userName, count = 0, Clock.utcNow().toString()))
    call.respondRedirect("/message")
}

fun PipelineContext<Unit, ApplicationCall>.handleUserSessionAuth(): UserSession? {
    val userSession = call.principal<UserSession>()
    call.sessions.set(userSession?.copy(count = userSession.count + 1))
    checkRequestCounter(userSession)
    return userSession
}

private fun PipelineContext<Unit, ApplicationCall>.checkRequestCounter(
    userSession: UserSession?
) {
    userSession?.let {
        if (isInLimitedTimeWindow(it)) {
            checkMaxRequests(it)
        } else {
            refreshToken(it)
        }
    }
}

private fun isInLimitedTimeWindow(it: UserSession) =
    Clock.utcNow() <= Clock.parse(it.token).plusSeconds(TTL_IN_SECONDS)

private fun checkMaxRequests(session: UserSession) {
    if (session.count > MAX_REQUESTS) throw UnauthorizedException()
}

private fun PipelineContext<Unit, ApplicationCall>.refreshToken(
    userSession: UserSession
) {
    call.sessions.set(userSession.copy(count = 1, token = Clock.utcNow().toString()))
}

fun SessionAuthenticationProvider.Config<UserSession>.handleAuthSession() {
    validate { session ->
        if (session.name.startsWith("lemon")) {
            session
        } else {
            null
        }
    }
    challenge {
        call.respondRedirect("/login")
    }
}

private const val TTL_IN_SECONDS = 10L
private const val MAX_REQUESTS = 5