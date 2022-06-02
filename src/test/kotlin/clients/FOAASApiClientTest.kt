package clients

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import exceptions.HttpInternalException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.runBlocking
import models.FOAASResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FOAASApiClientTest {

    private lateinit var engine: MockEngine
    private lateinit var foaasApiClient: FOAASApiClient

    @BeforeAll
    fun setup() {
        engine = MockEngine(config = MockEngineConfig().apply { addHandler { error("Unhandled error") } })
        foaasApiClient =
            FOAASApiClient(
                HttpClientConfiguration("localhost", "foaas"),
                HttpClient(engine) { expectSuccess = false },
            )
    }

    @BeforeEach
    fun clean() {
        engine.config.requestHandlers.clear()
    }

    @Test
    fun `getAwesomeMessage throws InternalException for unexpected error`() {
        val errorMessage = "Unhandled error"
        engine.config.addHandler { throw IllegalStateException(errorMessage) }

        val exception = assertThrows(HttpInternalException::class.java) {
            runBlocking { foaasApiClient.getAwesomeMessage("lautaro") }
        }
        assertEquals(
            "Error calling awesome/lautaro. Error detail [$errorMessage]",
            exception.message
        )
        assertEquals("client.unexpected.error", exception.code)
    }

    @Test
    fun `getAwesomeMessage success response`() {
        val countriesExpectedResponse = this.javaClass.getResource("/mocks/foaas-awesome-response.json").readText()
        engine.config.addHandler { respondOk(countriesExpectedResponse) }

        val response = runBlocking { foaasApiClient.getAwesomeMessage("lautaro") }

        assertEquals(
            response,
            jacksonObjectMapper().readValue(countriesExpectedResponse, FOAASResponse::class.java)
        )
    }
}
