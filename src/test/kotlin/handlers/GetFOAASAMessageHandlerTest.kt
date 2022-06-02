package handlers

import clients.FOAASApiClient
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import models.FOAASResponse
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetFOAASAMessageHandlerTest {

    private val userID = "lemon-cash"
    private val foaasApiClient = mockk<FOAASApiClient>()
    private val handler = GetFOAASMessageHandler(foaasApiClient)

    private val expectedFoaasResponse = FOAASResponse(
        message = "This is Fucking Awesome",
        subtitle = "- lemon"
    )

    @BeforeEach
    fun init() {
        clearMocks(foaasApiClient)
        coEvery { foaasApiClient.getAwesomeMessage(any()) } returns expectedFoaasResponse
    }

    @Test
    fun `should throw an exception when api client throws ex`() {
        coEvery { foaasApiClient.getAwesomeMessage(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { (handler(userID)) }
        }
    }

    @Test
    fun `should return FOAAS response`() {
        val result = assertDoesNotThrow {
            runBlocking { handler(userID) }
        }

        assertEquals(expectedFoaasResponse, result)

        coVerify(exactly = 1) {
            foaasApiClient.getAwesomeMessage(withArg { assertEquals(userID, it) })
        }
    }
}
