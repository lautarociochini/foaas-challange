import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

open class BaseRoutingTest {

    var koinModules: Module? = null
    var moduleList: Application.() -> Unit = { }

    fun <R> withBaseTestApplication(test: TestApplicationEngine.() -> R) {
        val env = createTestEnvironment {
            this.module {
                install(ContentNegotiation) { jackson { } }
                koinModules?.let {
                    install(Koin) {
                        modules(it)
                    }
                }
                moduleList()
            }
        }
        withApplication(env, {}, test)
    }
}
