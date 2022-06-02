package configuration

import io.ktor.server.config.*
import com.typesafe.config.Config as ConfigRetriever

class ServerConfig : ApplicationConfig {

    private val retriever = Config.getAll()

    override fun property(path: String): ApplicationConfigValue {
        if (!retriever.hasPath(path)) {
            throw ApplicationConfigurationException("Property $path not found.")
        }
        return ConfigValue(retriever, path)
    }

    override fun propertyOrNull(path: String): ApplicationConfigValue? {
        if (!retriever.hasPath(path)) {
            return null
        }
        return ConfigValue(retriever, path)
    }

    override fun configList(path: String): List<ApplicationConfig> {
        return retriever.getConfigList(path).map { HoconApplicationConfig(it) }
    }

    override fun keys(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun config(path: String): ApplicationConfig = ServerConfig()

    private class ConfigValue(val config: ConfigRetriever, val path: String) : ApplicationConfigValue {
        override fun getString(): String = config.getString(path)
        override fun getList(): List<String> = config.getStringList(path)
    }
}
