package cloud.horizonbot.webhookprocessor.routes

import cloud.horizonbot.webhookprocessor.config.Environment
import cloud.horizonbot.webhookprocessor.dto.TopGGPayload
import cloud.horizonbot.webhookprocessor.models.TopggVotesTable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.OffsetDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}
private val json = Json { ignoreUnknownKeys = true }

fun Routing.topGGRoutes() {
    post("/webhook/topgg") {
        val signatureHeader = call.request.header("x-topgg-signature")
        if (signatureHeader == null) {
            logger.warn { "Missing x-topgg-signature header" }
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val rawBody = call.receiveText()

        if (!verifySignature(Environment.topGGSecret, signatureHeader, rawBody)) {
            logger.warn { "Invalid top.gg webhook signature" }
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val payload = json.decodeFromString<TopGGPayload>(rawBody)

        if (payload.type == "webhook.test") {
            logger.info { "Received top.gg test webhook" }
            call.respond(HttpStatusCode.OK)
            return@post
        }

        if (payload.type != "vote.create" || payload.data == null) {
            call.respond(HttpStatusCode.OK)
            return@post
        }

        val data = payload.data
        val discordUserId = data.user.platformId.toLong()
        val votedAt = data.createdAt?.let { OffsetDateTime.parse(it).toInstant() } ?: Instant.now()
        val expiredAt = data.expiresAt?.let { OffsetDateTime.parse(it).toInstant() }
            ?: votedAt.plusSeconds(12 * 3600)

        transaction {
            TopggVotesTable.upsert {
                it[userId] = discordUserId
                it[platform] = "topgg"
it[TopggVotesTable.votedAt] = votedAt
                it[TopggVotesTable.expiredAt] = expiredAt
            }
        }

        logger.info { "Recorded top.gg vote from user $discordUserId (weight=${data.weight}, expires=$expiredAt)" }
        call.respond(HttpStatusCode.OK)
    }
}

private fun verifySignature(secret: String, header: String, rawBody: String): Boolean {
    val parts = header.split(",").associate {
        val (k, v) = it.split("=", limit = 2)
        k to v
    }
    val timestamp = parts["t"] ?: return false
    val receivedSig = parts["v1"] ?: return false

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
    val computed = mac.doFinal("$timestamp.$rawBody".toByteArray(Charsets.UTF_8))
    val computedHex = computed.joinToString("") { "%02x".format(it) }

    return computedHex == receivedSig
}
