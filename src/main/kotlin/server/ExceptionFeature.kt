package server

import exceptions.BaseException
import exceptions.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("exception-feature-logger")

fun Application.exceptions() {
    val messageKey = "message"
    val codeKey = "code"

    install(StatusPages) {
        exception<BaseException> { call, cause ->
            error(call, cause, HttpStatusCode.BadRequest)
        }
        exception<UnauthorizedException> { call, cause ->
            error(call, cause, HttpStatusCode.Unauthorized)
        }
        exception<IllegalArgumentException> { call, cause ->
            error(call, cause, HttpStatusCode.BadRequest)
        }
        exception<IllegalStateException> { call, cause ->
            error(call, cause, HttpStatusCode.BadRequest)
        }
        // unhandled errors
        exception<Exception> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                hashMapOf(messageKey to cause, codeKey to "unhandled.internal.exception")
            )
        }
        exception<Error> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                hashMapOf(messageKey to cause, codeKey to "unhandled.internal.error")
            )
        }
    }
}

private suspend fun error(
    call: ApplicationCall,
    it: BaseException,
    statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) {

    log.error(it.message, it) {
        "httpStatusCode" to statusCode
    }
    call.respond(statusCode, it.getMessageMap())
}

private suspend fun error(
    call: ApplicationCall,
    it: Exception,
    statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) {

    log.error(it.message, it) {
        "httpStatusCode" to statusCode
    }
    call.respond(statusCode, mapOf("message" to it.message))
}



