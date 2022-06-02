package exceptions

import io.kotlintest.shouldBe
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import BaseRoutingTest
import server.exceptions

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExceptionFeatureTest : BaseRoutingTest() {

    private val handler: TestHandler = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { handler }
        }

        moduleList = {
            routing()
            exceptions()
        }
    }

    private fun Application.routing() {
        install(Routing) {
            val handler by inject<TestHandler>()

            route("/test", HttpMethod.Get) {
                handle {
                    call.respondText(handler.handle())
                }
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(handler)
    }

    @Test
    fun `test exception feature with BaseException`() = withBaseTestApplication {
        coEvery { handler.handle() } throws BaseException("msg")

        val call = handleRequest(HttpMethod.Get, "test")

        with(call) {
            response.status() shouldBe HttpStatusCode.BadRequest
        }
    }

    @Test
    fun `test exception feature with UnauthorizedException`() = withBaseTestApplication {
        coEvery { handler.handle() } throws UnauthorizedException()

        val call = handleRequest(HttpMethod.Get, "test")

        with(call) {
            response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `test exception handler don't intercept successful response`() = withBaseTestApplication {
        val responseMessage = "Everything is ok"
        coEvery { handler.handle() } returns responseMessage

        val call = handleRequest(HttpMethod.Get, "test")

        with(call) {
            response.status() shouldBe HttpStatusCode.OK
            response.content shouldBe responseMessage
        }
    }

}

private class TestHandler {
    companion object {
        const val RESPONSE = "This is a test"
    }

    fun handle(): String {
        return RESPONSE
    }
}
