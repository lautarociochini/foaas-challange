import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.Server

fun main() {
        Server().apply {
            LoggerFactory.getLogger("Server").info("Starting server with empty cfg")
        }.start()
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass.name) }
}