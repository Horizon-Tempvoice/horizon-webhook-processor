package cloud.horizonbot.webhookprocessor

import cloud.horizonbot.webhookprocessor.config.DatabaseConfig
import cloud.horizonbot.webhookprocessor.routes.topGGRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

fun main() {
    DatabaseConfig.init()

    embeddedServer(Netty, port = 8080) {
        install(CallLogging)
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                logger.error(cause) { "Unhandled exception" }
                call.respond(io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        routing {
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }
            topGGRoutes()
        }
    }.start(wait = true)
}
