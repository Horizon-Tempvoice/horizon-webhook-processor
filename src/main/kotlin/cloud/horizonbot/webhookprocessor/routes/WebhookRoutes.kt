package cloud.horizonbot.webhookprocessor.routes

import cloud.horizonbot.webhookprocessor.core.ParseResult
import cloud.horizonbot.webhookprocessor.core.VerificationResult
import cloud.horizonbot.webhookprocessor.core.VoteProcessor
import cloud.horizonbot.webhookprocessor.core.WebhookAdapter
import cloud.horizonbot.webhookprocessor.core.WebhookRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

private val logger = KotlinLogging.logger {}

fun Routing.webhookRoutes(
    adapters: List<WebhookAdapter>,
    processor: VoteProcessor,
) {
    adapters.forEach { adapter ->
        post("/webhook/${adapter.path}") {
            val request = WebhookRequest(headers = { call.request.header(it) }, body = call.receiveText())

            val verification = adapter.verify(request)
            if (verification is VerificationResult.Rejected) {
                logger.warn { verification.reason }
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            when (val result = adapter.parse(request)) {
                is ParseResult.Accepted -> processor.process(result.event)
                ParseResult.Ignored -> Unit
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
