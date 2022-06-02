package clients

import io.ktor.client.HttpClient
import models.FOAASResponse

class FOAASApiClient(
    override val config: HttpClientConfiguration,
    override val client: HttpClient
) : AbstractHttpClient(config, client) {

    companion object {
        private const val BASE_URI = "awesome"
    }

    suspend fun getAwesomeMessage(from: String): FOAASResponse? {
        return get("$BASE_URI/$from", headers = mapOf("Accept" to "application/json"))!!
    }

}