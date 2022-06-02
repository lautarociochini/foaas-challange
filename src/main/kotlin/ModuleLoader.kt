import clients.FOAASApiClient
import clients.HttpClientConfiguration
import configuration.Config
import handlers.*
import io.ktor.client.*
import org.koin.core.module.Module
import org.koin.dsl.module

object ModuleLoader {

    private const val FOAAS_CLIENT_NAME = "foaas"

    fun initModule(): Module {
        return module
    }

    val module = module {
        initializeClients()
        injectHandlers()
    }


    private fun Module.initializeClients() {
        single {
            FOAASApiClient(
                HttpClientConfiguration(host = Config.get("client.foaas.host"), clientName = FOAAS_CLIENT_NAME),
                HttpClient {
                    engine {
                        threadsCount = 4
                        pipelining = true
                    }
                }
            )
        }

    }

    private fun Module.injectHandlers() {
        single { GetFOAASMessageHandler(get()) }
    }

}