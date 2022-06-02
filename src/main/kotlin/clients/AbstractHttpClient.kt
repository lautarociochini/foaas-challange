package clients

import exceptions.HttpInternalException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractHttpClient(
    open val config: HttpClientConfiguration,
    open val client: HttpClient
) {

    companion object {
        const val CLIENT_ERROR_CODE = "client.unexpected.error"
        val log: Logger = LoggerFactory.getLogger(this::class.java)
        val mapper: Mapper = Mapper.defaultCamelCaseMapper()
    }

    protected suspend inline fun <reified T> get(
        uri: String,
        params: Map<String, Any?> = mapOf(),
        headers: Map<String, Any?> = mapOf()
    ): T? {
        return exchange(HttpMethod.Get, uri, params, headers) { client.get(it) }
    }

    protected suspend inline fun <reified T> exchange(
        method: HttpMethod,
        uri: String,
        params: Map<String, Any?> = mapOf(),
        headers: Map<String, Any?> = mapOf(),
        crossinline clientOperation: suspend (HttpRequestBuilder) -> HttpResponse
    ): T? {
        var requestBuilder = HttpRequestBuilder()
        return try {
            requestBuilder = request(method = method, uri = uri, params = params, headers = headers)
            val response: HttpResponse = clientOperation(requestBuilder)
            val bytes = response.body<ByteArray>()
            log.debug("Request correctly executed: [$requestBuilder]")
            if (bytes.isEmpty().not()) mapper.deserialize(bytes) else null
        } catch (e: Exception) {
            log.error("Unmapped error calling $uri. with request: [$requestBuilder] and message ${e.message}")
            throw HttpInternalException("Error calling $uri. Error detail [${e.message}]", CLIENT_ERROR_CODE)
        }
    }

    protected fun request(
        method: HttpMethod,
        uri: String,
        params: Map<String, Any?>,
        headers: Map<String, Any?>
    ): HttpRequestBuilder {
        val requestBuilder = HttpRequestBuilder()
        requestBuilder.url("${config.host}$uri")
        requestBuilder.method = method

        params.entries.forEach { entry -> requestBuilder.parameter(entry.key, entry.value) }
        headers.entries.forEach { entry -> requestBuilder.header(entry.key, entry.value) }

        return requestBuilder
    }

}

