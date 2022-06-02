package configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object Config {

    private val properties = getProperties()

    fun getAll(): Config {
        return properties
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(path: String): T {
        return properties.getAnyRef(path) as T
    }

    private fun getProperties(): Config {
        val vaultConfig = ConfigFactory.empty()

        return ConfigFactory.parseResources("application.conf")
            .withFallback(vaultConfig)
            .resolve()
    }
}
