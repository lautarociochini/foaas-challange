package server

import ModuleLoader
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import configuration.ServerConfig
import configuration.Config
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import models.UserSession
import org.koin.ktor.plugin.Koin
import routes.routes

class Server {

    fun start() {
        val env = getApplicationEngineEnvironment()
        embeddedServer(Netty, env).start(wait = true)
    }

    private fun getApplicationEngineEnvironment(): ApplicationEngineEnvironment {
        return applicationEngineEnvironment {
            this.config = ServerConfig()
            this.module { main() }
            connector {
                this.host = Config.get("ktor.deployment.host")
                this.port = Config.get("ktor.deployment.port")
            }
        }
    }
}

private fun Application.main() {
    dependencies()
    exceptions()
    features()
}

private fun Application.features() {
    install(ContentNegotiation) {
        jackson {
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
            configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    install(Routing) {
        routes()
    }
    install(Sessions) {
        sessions()
    }
    install(Authentication) {
        authentication()
    }
}

private fun Application.dependencies() {
    install(Koin) {
        modules(
            ModuleLoader.module
        )
    }
}

fun SessionsConfig.sessions() {
    cookie<UserSession>("user_session") {
        cookie.path = "/"
        cookie.maxAgeInSeconds = 60
    }
}

fun AuthenticationConfig.authentication() {
    form("auth-form") {
        userParamName = "username"
        passwordParamName = "password"
        validate { credentials ->
            if (credentials.name.startsWith("lemon")  && credentials.password != "") {
                UserIdPrincipal(credentials.name)
            } else {
                null
            }
        }
    }
    session<UserSession>("auth-session") {
        handleAuthSession()
    }
}

