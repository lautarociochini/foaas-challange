package models

import io.ktor.server.auth.Principal

data class UserSession(val name: String, val count: Int, val token: String) : Principal