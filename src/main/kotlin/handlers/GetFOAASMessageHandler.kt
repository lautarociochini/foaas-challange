package handlers

import clients.FOAASApiClient
import logger
import models.FOAASResponse

class GetFOAASMessageHandler(
    private val foaasApiClient: FOAASApiClient
) {

    suspend operator fun invoke(user: String): FOAASResponse {
        return try {
            foaasApiClient.getAwesomeMessage(user)!!
        } catch (ex: Exception) {
            log.error("And error happened while trying to get FOAAS awesome message. Message: ${ex.message}")
            throw ex
        }
    }

    companion object {
        private val log by logger()
    }

}