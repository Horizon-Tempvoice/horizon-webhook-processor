package cloud.horizonbot.webhookprocessor

import cloud.horizonbot.webhookprocessor.adapters.topgg.TopGGAdapter
import cloud.horizonbot.webhookprocessor.adapters.topgg.TopGGSignatureVerifier
import cloud.horizonbot.webhookprocessor.config.DatabaseConfig
import cloud.horizonbot.webhookprocessor.config.Environment
import cloud.horizonbot.webhookprocessor.core.VoteProcessor
import cloud.horizonbot.webhookprocessor.core.WebhookAdapter
import cloud.horizonbot.webhookprocessor.notification.DiscordWebhookNotifier
import cloud.horizonbot.webhookprocessor.notification.VoteNotifier
import cloud.horizonbot.webhookprocessor.persistence.ExposedVoteRepository
import cloud.horizonbot.webhookprocessor.routes.webhookRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

private val logger = KotlinLogging.logger {}

fun main() {
    DatabaseConfig.init()

    val httpClient = HttpClient(CIO)

    val notifiers: List<VoteNotifier> =
        listOfNotNull(Environment.discordVoteWebhookUrl?.let { DiscordWebhookNotifier(httpClient, it) })

    val adapters: List<WebhookAdapter> =
        listOf(TopGGAdapter(TopGGSignatureVerifier(Environment.topGGSecret)))

    val processor = VoteProcessor(ExposedVoteRepository(), notifiers)

    embeddedServer(Netty, port = 8080) {
        install(CallLogging)
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                logger.error(cause) { "Unhandled exception" }
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        routing {
            get("/health") {
                call.respondText("ok")
            }
            webhookRoutes(adapters, processor)
        }
    }.start(wait = true)
}
