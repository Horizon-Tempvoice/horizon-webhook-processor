package cloud.horizonbot.webhookprocessor.routes

import cloud.horizonbot.webhookprocessor.config.Environment
import cloud.horizonbot.webhookprocessor.dto.TopGGPayload
import cloud.horizonbot.webhookprocessor.models.TopggVotesTable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

fun Routing.topGGRoutes() {
    post("/webhook/topgg") {
        val auth = call.request.header("Authorization")
        if (auth != Environment.topGGSecret) {
            logger.warn { "Unauthorized top.gg webhook request" }
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val payload = call.receive<TopGGPayload>()

        if (payload.type == "test") {
            logger.info { "Received top.gg test webhook from user ${payload.user}" }
            call.respond(HttpStatusCode.OK)
            return@post
        }

        val now = Instant.now()
        val validityHours = if (payload.isWeekend) 24L else 12L

        transaction {
            TopggVotesTable.upsert {
                it[userId] = payload.user.toLong()
                it[platform] = "topgg"
                it[votedAt] = now
                it[expiredAt] = now.plus(validityHours, ChronoUnit.HOURS)
            }
        }

        logger.info { "Recorded top.gg vote from user ${payload.user} (weekend=${payload.isWeekend}, valid=${validityHours}h)" }
        call.respond(HttpStatusCode.NoContent)
    }
}
