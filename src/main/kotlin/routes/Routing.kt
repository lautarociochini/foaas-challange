package routes

import handlers.GetFOAASMessageHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.koin.ktor.ext.inject
import server.handleUserSessionAuth
import server.setUserSession


fun Routing.routes() {

    route("/login") {
        get {
            call.respondHtml {
                body {
                    form(
                        action = "/login",
                        encType = FormEncType.applicationXWwwFormUrlEncoded,
                        method = FormMethod.post
                    ) {
                        p {
                            +"Username:"
                            textInput(name = "username")
                        }
                        p {
                            +"Password:"
                            passwordInput(name = "password")
                        }
                        p {
                            submitInput() { value = "Login" }
                        }
                    }
                }
            }
        }
    }

    authenticate("auth-form") {
        post("/login") {
            setUserSession()
        }
    }

    val getFOAASMessageHandler by inject<GetFOAASMessageHandler>()

    authenticate("auth-session") {
        get("/message") {
            val userSession = handleUserSessionAuth()
            call.respond(getFOAASMessageHandler.invoke(userSession!!.name))
        }
    }

}

