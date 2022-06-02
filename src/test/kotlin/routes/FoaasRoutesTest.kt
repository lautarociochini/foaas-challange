package routes

import BaseRoutingTest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import exceptions.UnauthorizedException
import handlers.GetFOAASMessageHandler
import io.kotlintest.shouldBe
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import models.FOAASResponse
import org.junit.jupiter.api.*
import org.koin.dsl.module
import server.authentication
import server.sessions
import utils.Clock
import utils.CookieGeneratorUtil.generateCookie
import utils.CookieGeneratorUtil.getTokenAsParam
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FoaasRoutesTest : BaseRoutingTest() {

    private val getFOAASMessageHandler: GetFOAASMessageHandler = mockk(relaxed = true)
    private val foaasResponse = FOAASResponse("This is Fucking Awesome", "- lemon")

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { getFOAASMessageHandler }
        }
        moduleList = {
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
    }

    @BeforeEach
    fun init() {
        coEvery { getFOAASMessageHandler(any()) } returns foaasResponse
    }

    @Test
    fun `getFoaasMessage returns 200`() = withBaseTestApplication {
        val token = getTokenAsParam()

        val call = handleRequest(HttpMethod.Get, "/message") {
            addHeader(HttpHeaders.Cookie, generateCookie(1, LEMON, token))
        }

        with(call) {
            response.status() shouldBe HttpStatusCode.OK
            response.content shouldBe jacksonObjectMapper().writeValueAsString(foaasResponse)
        }
    }

    @Test
    fun `getFoaasMessage five calls within 10 seconds returns 200`() = withBaseTestApplication {
        val token = getTokenAsParam()

        assertDoesNotThrow {
            for (i in 1..5) {
                handleRequest(HttpMethod.Get, "/message") {
                    addHeader(HttpHeaders.Cookie, generateCookie(i, LEMON, token))
                }
            }
        }
    }

    @Test
    fun `getFoaasMessage sixth call within 10 seconds returns unauthorized`() = withBaseTestApplication {
        val token = getTokenAsParam()

        for (i in 1..5) {
            handleRequest(HttpMethod.Get, "/message") {
                addHeader(HttpHeaders.Cookie, generateCookie(i, LEMON, token))
            }
        }

        val ex = assertThrows<UnauthorizedException> {
            handleRequest(HttpMethod.Get, "/message") {
                addHeader(HttpHeaders.Cookie, generateCookie(6, LEMON, token))
            }
        }
        assertEquals("You are not authorized to access this resource", ex.message)
    }

    @Test
    fun `getFoaasMessage sixth call returns 200 after 10 seconds`() = withBaseTestApplication {
        val token = getTokenAsParam()

        for (i in 1..5) {
            handleRequest(HttpMethod.Get, "/message") {
                addHeader(HttpHeaders.Cookie, generateCookie(i, LEMON, token))
            }
        }

        val expiredToken = getTokenAsParam(Clock.utcNow().minusSeconds(10))

        assertDoesNotThrow {
            handleRequest(HttpMethod.Get, "/message") {
                addHeader(HttpHeaders.Cookie, generateCookie(6, LEMON, expiredToken))
            }
        }
    }

    companion object {
        private const val LEMON = "lemon"
    }

}
